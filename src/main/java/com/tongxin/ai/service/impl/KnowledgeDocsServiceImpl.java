package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.KnowledgeDocDTO;
import com.tongxin.ai.entity.po.KnowledgeDocs;
import com.tongxin.ai.mapper.KnowledgeDocsMapper;
import com.tongxin.ai.service.IKnowledgeDocsService;
import com.tongxin.ai.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 知识库文档服务实现
 *
 * @author wyq
 * @since 2026-09-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocsServiceImpl extends ServiceImpl<KnowledgeDocsMapper, KnowledgeDocs> implements IKnowledgeDocsService {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 上传知识文档
     * 流程：解析文件 → 落库 knowledge_docs(status=0) → 向量化写入 → 更新 status=1
     * 向量化失败则删除 knowledge_docs 记录并抛业务异常
     */
    @Override
    @Transactional
    public String uploadDocument(MultipartFile file, String category, String title) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (!StringUtils.hasText(category)) {
            throw new BusinessException("文档分类不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名为空");
        }

        // 2. 提取文本内容
        String content = extractContent(file);
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("文档内容为空");
        }

        // 3. 生成业务字段
        String docId = UUID.randomUUID().toString().replace("-", "");
        String effectiveTitle = StringUtils.hasText(title) ? title : stripExtension(originalFilename);
        String summary = content.length() > 200 ? content.substring(0, 200) + "..." : content;

        // 4. 落库 knowledge_docs（先标记为 0 处理中）
        KnowledgeDocs doc = new KnowledgeDocs();
        doc.setDocId(docId);
        doc.setTitle(effectiveTitle);
        doc.setContent(content);
        doc.setSummary(summary);
        doc.setCategory(category);
        doc.setSource(originalFilename);
        doc.setStatus(0);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        this.save(doc);

        // 5. 向量化写入（失败时回滚 knowledge_docs 记录）
        try {
            knowledgeBaseService.ingestDocument(docId, content, effectiveTitle, category, originalFilename);
            doc.setStatus(1);
            doc.setUpdatedAt(LocalDateTime.now());
            this.updateById(doc);
            log.info("知识文档上传成功: docId={}, title={}, chunks已写入", docId, effectiveTitle);
        } catch (Exception e) {
            log.error("知识文档向量化失败, docId={}", docId, e);
            // 抛异常让 @Transactional 回滚 knowledge_docs 记录（向量分块此时可能尚未提交）
            throw new BusinessException("文档向量化失败：" + e.getMessage());
        }

        return docId;
    }

    /**
     * 分页查询知识库文档列表
     * 按 category + keyword(title/summary like) 过滤，按创建时间倒序
     */
    @Override
    public PageResult<KnowledgeDocDTO> getKnowledgeDocs(String category, String keyword, Integer pageNum, Integer pageSize) {
        Page<KnowledgeDocs> page = new Page<>(
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<KnowledgeDocs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), KnowledgeDocs::getCategory, category)
                .and(StringUtils.hasText(keyword),
                        w -> w.like(KnowledgeDocs::getTitle, keyword)
                                .or().like(KnowledgeDocs::getSummary, keyword))
                .orderByDesc(KnowledgeDocs::getCreatedAt);
        IPage<KnowledgeDocs> result = this.page(page, wrapper);

        List<KnowledgeDocs> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    Collections.emptyList(),
                    result.getTotal(),
                    result.getCurrent(),
                    result.getSize(),
                    result.getPages());
        }

        List<KnowledgeDocDTO> dtoList = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                dtoList,
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages());
    }

    /**
     * 删除知识文档
     * 先清理向量分块，再删 knowledge_docs 记录
     */
    @Override
    @Transactional
    public void deleteKnowledgeDoc(String docId) {
        KnowledgeDocs doc = this.getOne(new LambdaQueryWrapper<KnowledgeDocs>()
                .eq(KnowledgeDocs::getDocId, docId));
        if (doc == null) {
            throw new BusinessException(404, "知识文档不存在");
        }

        // 1. 先清理向量分块（走原生 SQL 删 ai_vector_store.metadata->>'docId' = ?）
        try {
            knowledgeBaseService.deleteByDocId(docId);
        } catch (Exception e) {
            log.warn("向量分块清理失败,将继续删除文档记录: docId={}", docId, e);
        }

        // 2. 删 knowledge_docs 记录
        this.removeById(doc.getId());
        log.info("知识文档删除成功: docId={}", docId);
    }

    /**
     * 从上传文件提取文本内容
     * 支持类型：txt/md（直接读字符串）、pdf（Spring AI PagePdfDocumentReader）
     */
    private String extractContent(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("无法识别文件类型");
        }
        String lower = filename.toLowerCase();
        try {
            if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
            if (lower.endsWith(".pdf")) {
                ByteArrayResource resource = new ByteArrayResource(file.getBytes());
                PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
                List<Document> docs = reader.get();
                return docs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n\n"));
            }
            throw new BusinessException("暂不支持的文件类型：" + filename + "（仅支持 txt/md/pdf）");
        } catch (IOException e) {
            log.error("读取文件失败: {}", filename, e);
            throw new BusinessException("读取文件失败：" + e.getMessage());
        }
    }

    /**
     * 去掉文件扩展名
     */
    private String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(0, idx) : filename;
    }

    /**
     * PO → DTO 转换（显式判空防止 BeanUtils.copyProperties 抛 IllegalArgumentException）
     * 不返回 content 大字段，节省传输开销
     */
    private KnowledgeDocDTO convertToDTO(KnowledgeDocs doc) {
        KnowledgeDocDTO dto = new KnowledgeDocDTO();
        if (doc == null) {
            return dto;
        }
        BeanUtils.copyProperties(doc, dto);
        return dto;
    }
}

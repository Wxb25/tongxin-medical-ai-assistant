package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.KnowledgeDocDTO;
import com.tongxin.ai.entity.po.KnowledgeDocs;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库文档服务接口
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IKnowledgeDocsService extends IService<KnowledgeDocs> {

    /**
     * 上传知识文档：解析文件内容 → 写入 knowledge_docs 表 → 向量化写入 ai_vector_store
     *
     * @param file     上传的文件（支持 txt/md/pdf）
     * @param category 分类：disease/drug/health
     * @param title    文档标题（可选，缺省时用文件名）
     * @return 新建文档的 docId
     */
    String uploadDocument(MultipartFile file, String category, String title);

    /**
     * 分页查询知识库文档列表
     *
     * @param category 分类（可选）
     * @param keyword  关键词（可选，匹配 title/summary）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<KnowledgeDocDTO> getKnowledgeDocs(String category, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 删除知识文档：先清理向量分块，再删除 knowledge_docs 记录
     *
     * @param docId 文档唯一标识
     */
    void deleteKnowledgeDoc(String docId);
}

package com.tongxin.ai.controller;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.dto.KnowledgeDocDTO;
import com.tongxin.ai.service.IKnowledgeDocsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 知识库管理控制器
 * 对应接口文档「知识库管理」tag，全部接口需 Bearer Token
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final IKnowledgeDocsService knowledgeDocsService;

    /**
     * 上传知识文档
     * POST /api/knowledge/upload  (multipart/form-data)
     *
     * @param file     文件（txt/md/pdf）
     * @param category 分类：disease/drug/health
     * @param title    文档标题（可选）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadKnowledge(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "title", required = false) String title) {
        String docId = knowledgeDocsService.uploadDocument(file, category, title);
        Map<String, String> data = new HashMap<>();
        data.put("docId", docId);
        return Result.success("上传成功", data);
    }

    /**
     * 查询知识库文档列表
     * GET /api/knowledge/docs
     */
    @GetMapping("/docs")
    public Result<PageResult<KnowledgeDocDTO>> getKnowledgeDocs(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<KnowledgeDocDTO> page = knowledgeDocsService.getKnowledgeDocs(category, keyword, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 删除知识文档
     * DELETE /api/knowledge/docs/{docId}
     */
    @DeleteMapping("/docs/{docId}")
    public Result<Void> deleteKnowledgeDoc(@PathVariable("docId") String docId) {
        knowledgeDocsService.deleteKnowledgeDoc(docId);
        return Result.success();
    }
}

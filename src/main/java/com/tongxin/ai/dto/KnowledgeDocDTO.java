package com.tongxin.ai.dto;

import lombok.Data;

/**
 * 知识库文档响应 DTO
 * 剔除纯运维字段（content 大字段、status、created_at/updated_at），仅返回前端列表/详情需要的元数据
 *
 * @author wyq
 */
@Data
public class KnowledgeDocDTO {

    /** 主键ID */
    private Long id;

    /** 文档唯一标识 */
    private String docId;

    /** 文档标题 */
    private String title;

    /** 文档摘要 */
    private String summary;

    /** 分类：disease疾病/drug药品/health健康 */
    private String category;

    /** 来源 */
    private String source;

    /** 向量嵌入ID（关联 ai_vector_store） */
    private String embeddingId;

    /** 状态：0处理中/1已启用/2已禁用 */
    private Integer status;
}

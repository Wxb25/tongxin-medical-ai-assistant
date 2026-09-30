package com.tongxin.ai.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("knowledge_docs")
public class KnowledgeDocs implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 文档唯一标识 */
    @TableField("doc_id")
    private String docId;

    /** 文档标题 */
    @TableField("title")
    private String title;

    /** 文档内容 */
    @TableField("content")
    private String content;

    /** 文档摘要 */
    @TableField("summary")
    private String summary;

    /** 分类：disease疾病/drug药品/health健康 */
    @TableField("category")
    private String category;

    /** 来源 */
    @TableField("source")
    private String source;

    /** 向量嵌入ID */
    @TableField("embedding_id")
    private String embeddingId;

    /** 状态：0处理中/1已启用/2已禁用 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}

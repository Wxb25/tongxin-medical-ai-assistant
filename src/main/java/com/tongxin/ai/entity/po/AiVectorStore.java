package com.tongxin.ai.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("ai_vector_store")
public class AiVectorStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /** 文本内容 */
    @TableField("content")
    private String content;

    /** 元数据信息 */
    @TableField("metadata")
    private String metadata;

    /** 向量嵌入数据 */
    @TableField("embedding")
    private String embedding;


}

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
@TableName("chat_messages")
public class ChatMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @TableField("session_id")
    private Long sessionId;

    /** 消息角色：user/assistant/system */
    @TableField("role")
    private String role;

    /** 消息内容 */
    @TableField("content")
    private String content;

    /** Token数量 */
    @TableField("token_count")
    private Integer tokenCount;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;


}

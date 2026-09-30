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
@TableName("chat_sessions")
public class ChatSessions implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会话唯一标识 */
    @TableField("session_id")
    private String sessionId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 会话标题 */
    @TableField("title")
    private String title;

    /** 上下文内容 */
    @TableField("context")
    private String context;

    /** 消息数量 */
    @TableField("message_count")
    private Integer messageCount;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}

package com.tongxin.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话会话响应 DTO
 *
 * @author wyq
 */
@Data
public class ChatSessionDTO {

    /** 主键ID */
    private Long id;

    /** 会话唯一标识 */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    /** 消息数量 */
    private Integer messageCount;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

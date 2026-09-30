package com.tongxin.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息响应 DTO
 *
 * @author wyq
 */
@Data
public class ChatMessageDTO {

    /** 主键ID */
    private Long id;

    /** 会话ID（chat_sessions.id） */
    private Long sessionId;

    /** 消息角色：user/assistant/system */
    private String role;

    /** 消息内容 */
    private String content;

    /** Token数量 */
    private Integer tokenCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}

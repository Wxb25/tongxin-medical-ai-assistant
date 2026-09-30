package com.tongxin.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 对话请求 DTO
 * 对应接口文档 ChatRequest schema
 *
 * @author wyq
 */
@Data
public class ChatRequest {

    /** 会话ID */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    /** 用户消息 */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /** 是否启用RAG检索增强（默认 false） */
    private Boolean useRag = false;
}

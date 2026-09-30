package com.tongxin.ai.dto;

import lombok.Data;

/**
 * 创建对话会话请求 DTO
 * 对应接口文档 POST /api/chat/sessions requestBody
 *
 * @author wyq
 */
@Data
public class CreateChatSessionRequest {

    /** 会话标题（可选，缺省时由后端默认生成） */
    private String title;
}

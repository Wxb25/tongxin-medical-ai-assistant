package com.tongxin.ai.controller;

import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.dto.ChatMessageDTO;
import com.tongxin.ai.dto.ChatRequest;
import com.tongxin.ai.dto.ChatSessionDTO;
import com.tongxin.ai.dto.CreateChatSessionRequest;
import com.tongxin.ai.entity.po.ChatSessions;
import com.tongxin.ai.service.AIChatService;
import com.tongxin.ai.service.IChatMessagesService;
import com.tongxin.ai.service.IChatSessionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 对话控制器
 * 对应接口文档「AI对话」tag。
 *
 * 鉴权说明（与 WebMvcConfig 对齐）：
 * - /api/chat/sessions/** 走 JWT 拦截器（创建/列表/删除/历史）
 * - /api/chat/stream 已在 WebMvcConfig 排除，不强制 JWT，靠 sessionId 自身鉴权
 *
 * @author wyq
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatSessionsService chatSessionsService;
    private final IChatMessagesService chatMessagesService;
    private final AIChatService aiChatService;

    /**
     * 创建对话会话
     * POST /api/chat/sessions
     */
    @PostMapping("/sessions")
    public Result<Map<String, String>> createSession(@RequestBody(required = false) CreateChatSessionRequest req) {
        Long userId = UserContext.getCurrentUserId();
        String sessionId = chatSessionsService.createSession(userId, req);
        Map<String, String> data = new HashMap<>();
        data.put("sessionId", sessionId);
        return Result.success("会话创建成功", data);
    }

    /**
     * 获取我的会话列表
     * GET /api/chat/sessions
     */
    @GetMapping("/sessions")
    public Result<PageResult<ChatSessionDTO>> getMySessions(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        PageResult<ChatSessionDTO> page = chatSessionsService.getMySessions(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 删除会话
     * DELETE /api/chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable("sessionId") String sessionId) {
        Long userId = UserContext.getCurrentUserId();
        chatSessionsService.deleteSession(userId, sessionId);
        return Result.success();
    }

    /**
     * 获取对话历史
     * GET /api/chat/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<PageResult<ChatMessageDTO>> getChatMessages(
            @PathVariable("sessionId") String sessionId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        // 业务 sessionId → 内部 id
        ChatSessions session = chatSessionsService.getBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在");
        }
        PageResult<ChatMessageDTO> page = chatMessagesService.getMessagesBySessionId(session.getId(), pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * AI 对话（SSE 流式）
     * POST /api/chat/stream
     *
     * 响应格式：text/event-stream，data 行携带 JSON 内容，最后发一个 [DONE] 事件标记结束
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest req) {
        // 0L = 无超时，等待 AI 完整推完
        SseEmitter emitter = new SseEmitter(0L);

        aiChatService.streamChat(req).subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (Exception e) {
                        log.warn("SSE 发送失败: sessionId={}", req.getSessionId(), e);
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("AI 对话流错误: sessionId={}", req.getSessionId(), error);
                    try {
                        // 业务异常以错误事件回写客户端
                        emitter.send(SseEmitter.event().name("error").data(error.getMessage()));
                    } catch (Exception ignored) {
                    }
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        // 标记流结束
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                }
        );

        // 客户端断开时清理
        emitter.onTimeout(emitter::complete);
        emitter.onError(throwable -> log.warn("SSE 连接异常: sessionId={}", req.getSessionId(), throwable));

        return emitter;
    }
}

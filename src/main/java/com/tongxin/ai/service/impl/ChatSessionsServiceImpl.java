package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.ChatSessionDTO;
import com.tongxin.ai.dto.CreateChatSessionRequest;
import com.tongxin.ai.entity.po.ChatMessages;
import com.tongxin.ai.entity.po.ChatSessions;
import com.tongxin.ai.mapper.ChatMessagesMapper;
import com.tongxin.ai.mapper.ChatSessionsMapper;
import com.tongxin.ai.service.IChatSessionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对话会话服务实现
 *
 * @author wyq
 * @since 2026-09-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionsServiceImpl extends ServiceImpl<ChatSessionsMapper, ChatSessions> implements IChatSessionsService {

    private final ChatMessagesMapper chatMessagesMapper;

    @Override
    @Transactional
    public String createSession(Long userId, CreateChatSessionRequest req) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        ChatSessions session = new ChatSessions();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setTitle(StringUtils.hasText(req == null ? null : req.getTitle()) ? req.getTitle() : "新对话");
        session.setMessageCount(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        this.save(session);
        log.info("创建会话成功: userId={}, sessionId={}", userId, sessionId);
        return sessionId;
    }

    @Override
    public PageResult<ChatSessionDTO> getMySessions(Long userId, Integer pageNum, Integer pageSize) {
        Page<ChatSessions> page = new Page<>(
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<ChatSessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSessions::getUserId, userId)
                .orderByDesc(ChatSessions::getUpdatedAt);
        IPage<ChatSessions> result = this.page(page, wrapper);

        List<ChatSessions> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    Collections.emptyList(),
                    result.getTotal(),
                    result.getCurrent(),
                    result.getSize(),
                    result.getPages());
        }
        List<ChatSessionDTO> dtoList = records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageResult<>(
                dtoList,
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages());
    }

    @Override
    @Transactional
    public void deleteSession(Long userId, String sessionId) {
        ChatSessions session = getBySessionId(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在");
        }
        // 校验归属（管理员场景暂不放开，仅允许本人删除自己的会话）
        if (userId != null && session.getUserId() != null && !session.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人会话");
        }
        // 1. 删消息
        chatMessagesMapper.delete(new LambdaQueryWrapper<ChatMessages>()
                .eq(ChatMessages::getSessionId, session.getId()));
        // 2. 删会话
        this.removeById(session.getId());
        log.info("删除会话成功: userId={}, sessionId={}", userId, sessionId);
    }

    @Override
    public ChatSessions getBySessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<ChatSessions>()
                .eq(ChatSessions::getSessionId, sessionId));
    }

    @Override
    public void incrementMessageCount(String sessionId, int delta) {
        ChatSessions session = getBySessionId(sessionId);
        if (session == null) {
            return;
        }
        int newCount = (session.getMessageCount() == null ? 0 : session.getMessageCount()) + delta;
        // 注意：chat_sessions.context 字段在数据库中是 jsonb 类型，
        // 直接 updateById 会把 String 当 VARCHAR 写入，触发 PostgreSQL 类型错误。
        // 改用 LambdaUpdateWrapper 只更新 message_count 和 updated_at，绕开 context 字段。
        this.update(new LambdaUpdateWrapper<ChatSessions>()
                .eq(ChatSessions::getSessionId, sessionId)
                .set(ChatSessions::getMessageCount, newCount)
                .set(ChatSessions::getUpdatedAt, LocalDateTime.now()));
    }

    /**
     * PO → DTO 转换（显式判空防止 BeanUtils.copyProperties 抛 IllegalArgumentException）
     */
    private ChatSessionDTO convertToDTO(ChatSessions session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        if (session == null) {
            return dto;
        }
        BeanUtils.copyProperties(session, dto);
        return dto;
    }
}

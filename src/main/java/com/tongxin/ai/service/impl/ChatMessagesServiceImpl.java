package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.ChatMessageDTO;
import com.tongxin.ai.entity.po.ChatMessages;
import com.tongxin.ai.mapper.ChatMessagesMapper;
import com.tongxin.ai.service.IChatMessagesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话消息服务实现
 *
 * @author wyq
 * @since 2026-09-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages> implements IChatMessagesService {

    @Override
    public PageResult<ChatMessageDTO> getMessagesBySessionId(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<ChatMessages> page = new Page<>(
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 20 : pageSize);
        LambdaQueryWrapper<ChatMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessages::getSessionId, sessionId)
                .orderByAsc(ChatMessages::getCreatedAt);
        IPage<ChatMessages> result = this.page(page, wrapper);

        List<ChatMessages> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    Collections.emptyList(),
                    result.getTotal(),
                    result.getCurrent(),
                    result.getSize(),
                    result.getPages());
        }
        List<ChatMessageDTO> dtoList = records.stream()
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
    public List<ChatMessages> getRecentMessages(Long sessionId, int limit) {
        // 先按时间倒序取 limit 条，再在内存里反转为升序，保证对话顺序
        LambdaQueryWrapper<ChatMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessages::getSessionId, sessionId)
                .orderByDesc(ChatMessages::getCreatedAt)
                .last("LIMIT " + Math.max(limit, 1));
        List<ChatMessages> desc = this.list(wrapper);
        if (desc == null || desc.isEmpty()) {
            return Collections.emptyList();
        }
        java.util.Collections.reverse(desc);
        return desc;
    }

    @Override
    public Long saveMessage(Long sessionId, String role, String content) {
        ChatMessages msg = new ChatMessages();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        this.save(msg);
        return msg.getId();
    }

    /**
     * PO → DTO 转换（显式判空防止 BeanUtils.copyProperties 抛 IllegalArgumentException）
     */
    private ChatMessageDTO convertToDTO(ChatMessages msg) {
        ChatMessageDTO dto = new ChatMessageDTO();
        if (msg == null) {
            return dto;
        }
        BeanUtils.copyProperties(msg, dto);
        return dto;
    }
}

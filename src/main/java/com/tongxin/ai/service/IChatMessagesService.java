package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.ChatMessageDTO;
import com.tongxin.ai.entity.po.ChatMessages;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 对话消息服务接口
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IChatMessagesService extends IService<ChatMessages> {

    /**
     * 分页查询会话消息（按时间升序，便于还原对话上下文）
     * @param sessionId 会话内部 ID（chat_sessions.id）
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    PageResult<ChatMessageDTO> getMessagesBySessionId(Long sessionId, Integer pageNum, Integer pageSize);

    /**
     * 查询会话最近 N 条消息（不分页，用于喂给 ChatClient 的历史上下文）
     * @param sessionId 会话内部 ID
     * @param limit     最多条数
     * @return 消息列表（按时间升序）
     */
    List<ChatMessages> getRecentMessages(Long sessionId, int limit);

    /**
     * 保存一条消息
     * @param sessionId 会话内部 ID
     * @param role      角色（user/assistant/system）
     * @param content   消息内容
     * @return 新消息的主键 ID
     */
    Long saveMessage(Long sessionId, String role, String content);
}

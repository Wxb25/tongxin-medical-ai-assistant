package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.ChatSessionDTO;
import com.tongxin.ai.dto.CreateChatSessionRequest;
import com.tongxin.ai.entity.po.ChatSessions;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 对话会话服务接口
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IChatSessionsService extends IService<ChatSessions> {

    /**
     * 创建对话会话
     * @param userId 当前登录用户ID
     * @param req    请求体（可选 title）
     * @return 新建会话的业务 sessionId
     */
    String createSession(Long userId, CreateChatSessionRequest req);

    /**
     * 分页查询当前用户的会话列表
     * @param userId   当前登录用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<ChatSessionDTO> getMySessions(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 删除会话（同时清理消息记录）
     * @param userId    当前登录用户ID
     * @param sessionId 会话业务标识
     */
    void deleteSession(Long userId, String sessionId);

    /**
     * 按业务 sessionId 查询会话实体（供 AIChatService 校验 session 用）
     * @param sessionId 业务标识
     * @return 会话实体，不存在返回 null
     */
    ChatSessions getBySessionId(String sessionId);

    /**
     * 更新会话消息计数与更新时间（供 AIChatService 在对话完成后调用）
     * @param sessionId 业务标识
     * @param delta     增加的消息条数
     */
    void incrementMessageCount(String sessionId, int delta);
}

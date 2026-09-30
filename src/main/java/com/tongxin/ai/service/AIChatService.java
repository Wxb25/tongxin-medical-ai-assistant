package com.tongxin.ai.service;

import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.dto.ChatRequest;
import com.tongxin.ai.entity.po.ChatMessages;
import com.tongxin.ai.entity.po.ChatSessions;
import com.tongxin.ai.service.tools.AppointmentBookingTool;
import com.tongxin.ai.service.tools.DoctorQueryTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * AI 对话服务
 * 封装 SSE 流式响应、历史上下文拼接、RAG 检索增强、工具调用、消息持久化。
 *
 * 工具调用（Function Calling）：
 * - queryDoctors：查询数据库中的真实医生，防止 AI 编造
 * - getDoctorDetail：查看医生详情
 * - getDoctorSchedule：查看医生排班号源
 * - bookAppointment：帮助病人创建预约
 *
 * 设计要点：
 * 1. 使用 Spring AI 的 stream().content() 真流式输出，逐 token 推给前端（打字机效果）
 * 2. Spring AI 自动完成工具调用循环（AI 发起 tool call → 执行 → 回填 → 继续生成）
 * 3. RAG：useRag=true 时先调 KnowledgeBaseService.searchRelevant 取 top-K 文档片段拼进 system prompt
 * 4. 流式过程中累积完整回复，流结束时一次性持久化到数据库
 *
 * @author wyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    /** 历史上下文最多回溯条数 */
    private static final int HISTORY_LIMIT = 10;
    /** RAG 检索 top-K */
    private static final int RAG_TOP_K = 4;

    /** 默认医学系统提示词 */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是同心医院AI智能助手，专注于医疗咨询和预约挂号。请用中文专业、简洁地回答用户问题。\n" +
            "\n" +
            "核心能力：\n" +
            "1. 回答医疗健康相关问题（注意：不得给出明确诊断或具体药物处方，必要时引导患者到院就诊）\n" +
            "2. 查询医院真实医生信息（通过 queryDoctors / getDoctorDetail 工具，数据来自数据库，不要编造）\n" +
            "3. 查看医生排班和剩余号源（通过 getDoctorSchedule 工具）\n" +
            "4. 帮助病人预约挂号（通过 bookAppointment 工具，需要医生ID、日期和时段）\n" +
            "\n" +
            "使用工具的规则：\n" +
            "- 当用户询问医生信息、科室、挂号费等时，必须调用 queryDoctors 查询真实数据，绝对不能自己编造医生姓名或信息\n" +
            "- 当用户想预约时，先查医生和排班，确认有号后再调用 bookAppointment\n" +
            "- 预约时段格式为 HH:mm-HH:mm（如 09:00-09:30），每天上午 08:00-12:00、下午 14:00-17:30 可预约\n" +
            "- 如果用户信息不完整（缺少医生ID、日期或时段），请主动询问，不要盲目调用工具\n" +
            "- 回复时使用自然语言，不要直接暴露工具返回的原始文本\n" +
            "\n" +
            "重要：\n" +
            "- 用户是在同心医院前端网站（浏览器）上与你对话，不存在公众号、APP、小程序等其他登录渠道\n" +
            "- 如果 bookAppointment 工具返回未登录提示，告诉用户：请在当前网站完成登录（点击右上角登录按钮），登录成功后再次发起预约即可\n" +
            "- 绝对不要编造公众号、APP、小程序、二维码等不存在的登录方式\n" +
            "- 关于就诊地点：绝对不要编造具体的门诊室号、楼层、房间号等信息。如果用户问在哪里就诊，请根据用户咨询或预约的科室，回复\"请前往同心医院{科室名}门诊室就诊\"，例如用户挂的是内科就说\"请前往同心医院内科门诊室就诊\"，不要说具体几号诊室、几楼等你不确定的信息\n" +
            "- 所有医生姓名、科室、专长、挂号费等信息必须通过 queryDoctors 工具从数据库获取，严禁编造";

    private final ChatClient.Builder chatClientBuilder;
    private final IChatSessionsService chatSessionsService;
    private final IChatMessagesService chatMessagesService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final DoctorQueryTool doctorQueryTool;
    private final AppointmentBookingTool appointmentBookingTool;

    /** 构造完成后 build 一次复用 */
    private ChatClient chatClient;

    @PostConstruct
    private void initChatClient() {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 流式对话（真 SSE 流式）
     * 使用 Spring AI stream().content() 逐 token 输出，前端呈现打字机效果。
     * 工具调用循环由 Spring AI 自动处理。
     *
     * @param req 请求体（sessionId/message/useRag）
     * @return AI 回复内容的增量字符流
     */
    public Flux<String> streamChat(ChatRequest req) {
        // 1. 校验会话
        ChatSessions session = chatSessionsService.getBySessionId(req.getSessionId());
        if (session == null) {
            return Flux.error(new BusinessException(404, "会话不存在"));
        }

        // 2. 加载历史消息
        List<ChatMessages> history = chatMessagesService.getRecentMessages(session.getId(), HISTORY_LIMIT);
        List<Message> historyMessages = history.stream()
                .map(m -> "assistant".equals(m.getRole())
                        ? new AssistantMessage(m.getContent())
                        : new UserMessage(m.getContent()))
                .collect(Collectors.toList());

        // 3. 持久化用户消息（同步，确保即使用户中断也有记录）
        chatMessagesService.saveMessage(session.getId(), "user", req.getMessage());

        // 4. 构建 system prompt（含 RAG 检索）
        String systemPrompt = buildSystemPrompt(req);

        // 5. 累积完整回复，流结束时持久化
        AtomicReference<StringBuilder> fullReply = new AtomicReference<>(new StringBuilder());

        // 6. 真流式调用：stream().content() 返回增量 Flux<String>
        //    Spring AI 自动处理工具调用循环（tool call → 执行 → 回填 → 继续生成）
        Flux<String> contentFlux = chatClient.prompt()
                .system(systemPrompt)
                .messages(historyMessages)
                .user(req.getMessage())
                .tools(doctorQueryTool, appointmentBookingTool)
                .stream()
                .content();

        return contentFlux
                .doOnNext(chunk -> {
                    if (chunk != null && !chunk.isEmpty()) {
                        fullReply.get().append(chunk);
                    }
                })
                .doOnComplete(() -> {
                    String reply = fullReply.get().toString();
                    if (!reply.isBlank()) {
                        chatMessagesService.saveMessage(session.getId(), "assistant", reply);
                        chatSessionsService.incrementMessageCount(req.getSessionId(), 2);
                        log.info("AI 流式回复完成: sessionId={}, replyLen={}", req.getSessionId(), reply.length());
                    }
                })
                .doOnError(error -> log.error("AI 流式对话失败: sessionId={}", req.getSessionId(), error));
    }

    /**
     * 构建 system prompt
     * RAG 模式：检索知识库相关片段，拼进 prompt 上下文
     */
    private String buildSystemPrompt(ChatRequest req) {
        if (Boolean.TRUE.equals(req.getUseRag())) {
            List<String> relevant = knowledgeBaseService.searchRelevant(req.getMessage(), RAG_TOP_K);
            if (relevant.isEmpty()) {
                return DEFAULT_SYSTEM_PROMPT;
            }
            String context = String.join("\n---\n", relevant);
            return DEFAULT_SYSTEM_PROMPT
                    + "\n\n以下是知识库中可能相关的内容，请结合它回答用户问题，"
                    + "若与问题无关则忽略：\n"
                    + context;
        }
        return DEFAULT_SYSTEM_PROMPT;
    }
}

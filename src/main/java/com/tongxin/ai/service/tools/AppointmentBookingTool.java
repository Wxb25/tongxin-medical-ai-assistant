package com.tongxin.ai.service.tools;

import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.dto.CreateAppointmentRequest;
import com.tongxin.ai.service.IAppointmentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * AI 工具：帮助病人预约挂号
 * 调用真实的预约服务，基于数据库创建预约记录
 *
 * @author wyq
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentBookingTool {

    private final IAppointmentsService appointmentsService;

    /**
     * 创建预约
     * 时段格式必须为 HH:mm-HH:mm，如 09:00-09:30，跨度不超过 30 分钟
     *
     * @param doctorId        医生ID（先从 queryDoctors 获取）
     * @param appointmentDate 预约日期，格式 yyyy-MM-dd
     * @param appointmentTime 预约时段，格式 HH:mm-HH:mm，例如 09:00-09:30
     * @param symptom         症状描述（可选）
     */
    @Tool(name = "bookAppointment", description = "帮助病人创建预约挂号。需要提供医生ID、日期和时段。时段格式为 HH:mm-HH:mm（如 09:00-09:30），每天上午 08:00-12:00、下午 14:00-17:30 可预约。")
    public String bookAppointment(
            @ToolParam(description = "医生ID，从 queryDoctors 返回结果中获取，例如 1、2、3") Long doctorId,
            @ToolParam(description = "预约日期，格式 yyyy-MM-dd，例如 2026-09-18。最多可提前 7 天") String appointmentDate,
            @ToolParam(description = "预约时段，格式 HH:mm-HH:mm，例如 09:00-09:30。每天 08:00-12:00 和 14:00-17:30 有号") String appointmentTime,
            @ToolParam(description = "症状描述，可选，例如：咳嗽三天、头痛等") String symptom) {
        try {
            // 1. 取当前登录用户ID
            Long patientId = UserContext.getCurrentUserId();
            if (patientId == null) {
                return "预约失败：当前用户未登录。请让用户在同心医院前端网站完成登录（点击右上角登录按钮），登录成功后再次发起预约即可。" +
                        "本系统没有公众号、APP、小程序等其他登录渠道，不要建议用户使用这些方式。";
            }

            // 2. 解析日期
            LocalDate date;
            try {
                date = LocalDate.parse(appointmentDate);
            } catch (DateTimeParseException e) {
                return "预约失败：日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2026-09-18。";
            }

            // 3. 校验时段格式
            if (appointmentTime == null || !appointmentTime.matches("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")) {
                return "预约失败：时段格式不正确，必须是 HH:mm-HH:mm，例如 09:00-09:30。";
            }

            // 4. 构建请求并调用预约服务
            CreateAppointmentRequest req = new CreateAppointmentRequest();
            req.setDoctorId(doctorId);
            req.setAppointmentDate(date);
            req.setAppointmentTime(appointmentTime);
            req.setSymptom(symptom);

            Long appointmentId = appointmentsService.createAppointment(patientId, req);
            return "预约成功！\n"
                    + "预约编号：" + appointmentId + "\n"
                    + "医生ID：" + doctorId + "\n"
                    + "日期：" + appointmentDate + "\n"
                    + "时段：" + appointmentTime + "\n"
                    + "症状：" + (symptom == null || symptom.isEmpty() ? "无" : symptom) + "\n"
                    + "请到「我的预约」页面查看预约详情。如需取消，也可在该页面操作。";

        } catch (BusinessException e) {
            // 业务异常直接返回给 AI 转告用户
            return "预约失败：" + e.getMessage();
        } catch (Exception e) {
            log.error("AI 调用 bookAppointment 失败", e);
            return "预约失败：系统内部错误，请稍后再试或直接到预约挂号页面操作。";
        }
    }
}

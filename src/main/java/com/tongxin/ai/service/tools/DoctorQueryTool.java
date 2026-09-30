package com.tongxin.ai.service.tools;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.DoctorDTO;
import com.tongxin.ai.dto.DoctorScheduleDTO;
import com.tongxin.ai.service.IDoctorsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 工具：查询数据库中的真实医生信息
 * 防止 AI 编造不存在的医生，所有医生数据均来自数据库
 *
 * 提供三个方法：
 * 1. queryDoctors       - 按科室/关键词搜索医生
 * 2. getDoctorDetail    - 查看医生详情
 * 3. getDoctorSchedule  - 查看医生某日期的排班号源
 *
 * @author wyq
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorQueryTool {

    private final IDoctorsService doctorsService;

    /**
     * 按科室或关键词搜索医生（返回前 10 条）
     * @param department 科室名称，如"内科"、"外科"，可为空
     * @param keyword    关键词（医生姓名/专长），可为空
     * @return 医生列表文本
     */
    @Tool(name = "queryDoctors", description = "查询同心医院的医生列表，可以按科室或关键词搜索。返回真实医生数据，包含姓名、科室、职称、专长、挂号费、评分。")
    public String queryDoctors(
            @ToolParam(description = "科室名称，例如：内科、外科、妇产科、儿科等。如果不确定科室可留空") String department,
            @ToolParam(description = "关键词，可搜索医生姓名或擅长领域，例如：王医生、高血压、骨折。可为空") String keyword) {
        try {
            PageResult<DoctorDTO> result = doctorsService.searchDoctors(
                    department, keyword, 1, 10);
            if (result.getRecords().isEmpty()) {
                return "未查询到符合条件的医生，请确认科室名称或关键词是否正确。同心医院科室包括：内科、外科、妇产科、儿科、骨科、眼科、口腔科、皮肤科、中医科、康复科等。";
            }
            StringBuilder sb = new StringBuilder("查询到以下医生（数据来自医院数据库，真实有效）：\n\n");
            int i = 1;
            for (DoctorDTO d : result.getRecords()) {
                sb.append(i++).append(". ").append(d.getName())
                        .append("（").append(d.getTitle()).append("）\n")
                        .append("   科室：").append(d.getDepartment()).append("\n")
                        .append("   专长：").append(d.getSpecialty() == null ? "暂无" : d.getSpecialty()).append("\n")
                        .append("   挂号费：¥").append(d.getConsultationFee()).append("\n")
                        .append("   医生ID：").append(d.getId()).append("\n\n");
            }
            sb.append("如需查看某医生的详细排班，请提供医生ID调用 getDoctorSchedule 工具。");
            return sb.toString();
        } catch (Exception e) {
            log.error("AI 调用 queryDoctors 失败", e);
            return "查询医生时发生错误，请稍后再试。";
        }
    }

    /**
     * 查看医生详情
     * @param doctorId 医生ID（从 queryDoctors 结果中获取）
     */
    @Tool(name = "getDoctorDetail", description = "查看指定医生的详细信息，包括个人简介、擅长领域等。需要先通过 queryDoctors 获取医生ID。")
    public String getDoctorDetail(
            @ToolParam(description = "医生ID，从 queryDoctors 的返回结果中获取，例如 1、2、3") Long doctorId) {
        try {
            DoctorDTO d = doctorsService.getDoctorDetail(doctorId);
            if (d == null) {
                return "未找到该医生，请确认医生ID是否正确。";
            }
            return "医生详情：\n"
                    + "姓名：" + d.getName() + "（" + d.getTitle() + "）\n"
                    + "科室：" + d.getDepartment() + "\n"
                    + "专长：" + (d.getSpecialty() == null ? "暂无" : d.getSpecialty()) + "\n"
                    + "简介：" + (d.getIntroduction() == null ? "暂无" : d.getIntroduction()) + "\n"
                    + "挂号费：¥" + d.getConsultationFee();
        } catch (Exception e) {
            log.error("AI 调用 getDoctorDetail 失败", e);
            return "查询医生详情时发生错误。";
        }
    }

    /**
     * 查看医生某日期的排班号源
     * @param doctorId 医生ID
     * @param date     日期，格式 yyyy-MM-dd，例如 2026-09-18。最多可查询未来 7 天
     */
    @Tool(name = "getDoctorSchedule", description = "查看医生在指定日期的排班和剩余号源。每个时段上限10人，返回各时段剩余号数。")
    public String getDoctorSchedule(
            @ToolParam(description = "医生ID，从 queryDoctors 的返回结果中获取") Long doctorId,
            @ToolParam(description = "预约日期，格式 yyyy-MM-dd，例如 2026-09-18。最多可提前 7 天预约") String date) {
        try {
            LocalDate target = LocalDate.parse(date);
            List<DoctorScheduleDTO> schedule = doctorsService.getDoctorSchedule(doctorId, target);
            if (schedule.isEmpty()) {
                return "该医生在 " + date + " 暂无排班，请选择其他日期。";
            }
            // 按时段分组（上午/下午）
            List<DoctorScheduleDTO> morning = schedule.stream()
                    .filter(s -> s.getTimeSlot().startsWith("08") || s.getTimeSlot().startsWith("09")
                            || s.getTimeSlot().startsWith("10") || s.getTimeSlot().startsWith("11"))
                    .collect(Collectors.toList());
            List<DoctorScheduleDTO> afternoon = schedule.stream()
                    .filter(s -> s.getTimeSlot().startsWith("14") || s.getTimeSlot().startsWith("15")
                            || s.getTimeSlot().startsWith("16") || s.getTimeSlot().startsWith("17"))
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder("医生 ").append(doctorId).append(" 在 ").append(date).append(" 的排班：\n\n");
            sb.append("【上午 08:00-12:00】\n");
            for (DoctorScheduleDTO s : morning) {
                sb.append("  ").append(s.getTimeSlot())
                        .append(" → 剩余 ").append(s.getRemaining()).append(" 个号\n");
            }
            sb.append("\n【下午 14:00-17:30】\n");
            for (DoctorScheduleDTO s : afternoon) {
                sb.append("  ").append(s.getTimeSlot())
                        .append(" → 剩余 ").append(s.getRemaining()).append(" 个号\n");
            }
            sb.append("\n提示：如果需要预约，请提供医生ID、日期和时段（如 09:00-09:30）调用 bookAppointment 工具。");
            return sb.toString();
        } catch (Exception e) {
            log.error("AI 调用 getDoctorSchedule 失败", e);
            return "查询排班时发生错误，请确认日期格式为 yyyy-MM-dd。";
        }
    }
}

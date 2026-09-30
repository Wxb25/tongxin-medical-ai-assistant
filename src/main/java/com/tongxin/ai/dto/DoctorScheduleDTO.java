package com.tongxin.ai.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 医生排班响应 DTO（含剩余号源计算结果）
 * 字段对齐前端 ScheduleItem：date / timeSlot / total / remaining
 *
 * 规则：只要医生 status=1，按固定规则生成时段
 *   上午 08:00-12:00，每 30 分钟一段，共 8 段
 *   下午 14:00-17:30，每 30 分钟一段，共 7 段
 *   每段上限 10 个号源，remaining = 10 - 已预约数
 *
 * @author wyq
 */
@Data
public class DoctorScheduleDTO {

    /** 具体日期 */
    private LocalDate date;

    /** 时段，格式 HH:mm-HH:mm，例如 08:00-08:30 */
    private String timeSlot;

    /** 该时段号源总数（固定 10） */
    private Integer total;

    /** 已预约数（来自 appointments 表实时统计，已取消 status=2 不计） */
    private Integer booked;

    /** 剩余号源 = total - booked（不小于 0） */
    private Integer remaining;
}

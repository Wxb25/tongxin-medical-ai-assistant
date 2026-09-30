package com.tongxin.ai.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建预约请求 DTO
 * 对应接口文档 CreateAppointmentRequest schema
 *
 * @author wyq
 */
@Data
public class CreateAppointmentRequest {

    /** 医生ID */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    /** 预约日期 */
    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    /** 预约时段，格式 HH:mm-HH:mm，且时间差不超过 30 分钟（如 09:00-09:30） */
    @NotBlank(message = "预约时段不能为空")
    @Pattern(regexp = "^\\d{2}:\\d{2}-\\d{2}:\\d{2}$", message = "预约时段格式必须为 HH:mm-HH:mm，例如 09:00-09:30")
    private String appointmentTime;

    /** 症状描述（可选） */
    private String symptom;
}

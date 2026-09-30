package com.tongxin.ai.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 预约响应 DTO
 * 剔除纯运维字段（created_at/updated_at），并拼接医生姓名/科室供前端直接展示
 * 注：status 对预约来说是核心业务字段（待确认/已取消等），需保留
 *
 * @author wyq
 */
@Data
public class AppointmentDTO {

    /** 预约ID */
    private Long id;

    /** 患者ID */
    private Long patientId;

    /** 医生ID */
    private Long doctorId;

    /** 医生姓名（关联 doctors 表） */
    private String doctorName;

    /** 所属科室（关联 doctors 表） */
    private String department;

    /** 预约日期 */
    private LocalDate appointmentDate;

    /** 预约时段 */
    private String appointmentTime;

    /** 症状描述 */
    private String symptom;

    /** 预约状态：0待确认/1已确认/2已取消/3已完成 */
    private Integer status;

    /** 取消原因 */
    private String cancelReason;
}

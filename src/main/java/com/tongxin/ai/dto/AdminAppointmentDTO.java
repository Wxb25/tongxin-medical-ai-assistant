package com.tongxin.ai.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理端预约 DTO
 * 包含患者姓名、医生姓名、科室，便于管理端展示
 *
 * @author wyq
 */
@Data
public class AdminAppointmentDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String department;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String symptom;
    /** 0已预约/1已预约/2已取消/3已完成（兼容历史） */
    private Integer status;
    private String cancelReason;
    private LocalDateTime createdAt;
}

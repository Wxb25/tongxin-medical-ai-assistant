package com.tongxin.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 取消预约请求 DTO
 * 对应接口文档 PUT /api/appointments/{appointmentId}/cancel 的 requestBody
 *
 * @author wyq
 */
@Data
public class CancelAppointmentRequest {

    /** 取消原因 */
    @NotBlank(message = "取消原因不能为空")
    private String cancelReason;
}

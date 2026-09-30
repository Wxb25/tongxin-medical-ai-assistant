package com.tongxin.ai.controller;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.common.UserContext;
import com.tongxin.ai.dto.AppointmentDTO;
import com.tongxin.ai.dto.CancelAppointmentRequest;
import com.tongxin.ai.dto.CreateAppointmentRequest;
import com.tongxin.ai.service.IAppointmentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 预约管理控制器
 * 对应接口文档「预约管理」tag，全部接口需 Bearer Token
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentsService appointmentsService;

    /**
     * 创建预约
     * POST /api/appointments
     */
    @PostMapping
    public Result<Map<String, Long>> createAppointment(@Valid @RequestBody CreateAppointmentRequest req) {
        Long patientId = UserContext.getCurrentUserId();
        Long appointmentId = appointmentsService.createAppointment(patientId, req);
        Map<String, Long> data = new HashMap<>();
        data.put("appointmentId", appointmentId);
        return Result.success("预约创建成功", data);
    }

    /**
     * 获取我的预约列表
     * GET /api/appointments/my
     */
    @GetMapping("/my")
    public Result<PageResult<AppointmentDTO>> getMyAppointments(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long patientId = UserContext.getCurrentUserId();
        PageResult<AppointmentDTO> page = appointmentsService.getMyAppointments(patientId, status, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 取消预约
     * PUT /api/appointments/{appointmentId}/cancel
     */
    @PutMapping("/{appointmentId}/cancel")
    public Result<Void> cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
                                          @Valid @RequestBody CancelAppointmentRequest req) {
        Long patientId = UserContext.getCurrentUserId();
        appointmentsService.cancelAppointment(patientId, appointmentId, req);
        return Result.success();
    }
}

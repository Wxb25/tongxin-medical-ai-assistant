package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.AppointmentDTO;
import com.tongxin.ai.dto.CancelAppointmentRequest;
import com.tongxin.ai.dto.CreateAppointmentRequest;
import com.tongxin.ai.entity.po.Appointments;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 预约服务接口
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IAppointmentsService extends IService<Appointments> {

    /**
     * 创建预约
     * @param patientId 当前登录患者ID
     * @param req       请求体
     * @return 新建的预约ID
     */
    Long createAppointment(Long patientId, CreateAppointmentRequest req);

    /**
     * 分页查询当前用户的预约列表
     * @param patientId 当前登录患者ID
     * @param status    预约状态（可选，传 null 查全部）
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    PageResult<AppointmentDTO> getMyAppointments(Long patientId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 取消预约
     * @param patientId      当前登录患者ID
     * @param appointmentId  预约ID
     * @param req            取消请求（含取消原因）
     */
    void cancelAppointment(Long patientId, Long appointmentId, CancelAppointmentRequest req);
}

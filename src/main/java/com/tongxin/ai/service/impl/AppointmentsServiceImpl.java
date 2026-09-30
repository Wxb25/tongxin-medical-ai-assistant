package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.AppointmentDTO;
import com.tongxin.ai.dto.CancelAppointmentRequest;
import com.tongxin.ai.dto.CreateAppointmentRequest;
import com.tongxin.ai.entity.po.Appointments;
import com.tongxin.ai.entity.po.Doctors;
import com.tongxin.ai.mapper.AppointmentsMapper;
import com.tongxin.ai.mapper.DoctorsMapper;
import com.tongxin.ai.service.IAppointmentsService;
import com.tongxin.ai.service.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预约服务实现
 *
 * @author wyq
 * @since 2026-09-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentsServiceImpl extends ServiceImpl<AppointmentsMapper, Appointments> implements IAppointmentsService {

    private final DoctorsMapper doctorsMapper;
    private final RedisLockService redisLockService;

    /** 预约分布式锁 TTL（秒），来自配置 cache.appointment-lock-ttl */
    @Value("${cache.appointment-lock-ttl:10}")
    private long appointmentLockTtl;

    /**
     * 创建预约
     * 使用 Redis 分布式锁防止并发超卖：同一医生+日期+时段的预约请求串行化。
     * 锁内完成：校验医生 → 校验日期 → 重复预约校验 → 号源上限校验 → 落库。
     */
    @Override
    @Transactional
    public Long createAppointment(Long patientId, CreateAppointmentRequest req) {
        // 锁 key：精确到医生+日期+时段，粒度最小化，不影响其他时段并发
        String lockKey = String.format("lock:appointment:%d:%s:%s",
                req.getDoctorId(), req.getAppointmentDate(), req.getAppointmentTime());

        return redisLockService.executeWithLock(lockKey, appointmentLockTtl,
                "该时段预约繁忙，请稍后重试",
                () -> doCreateAppointment(patientId, req));
    }

    /**
     * 预约创建核心逻辑（在分布式锁内执行）
     * 1. 校验预约时段格式
     * 2. 校验医生存在且启用
     * 3. 校验预约日期不能是过去
     * 4. 重复预约校验（同患者+同医生+同日期+同时段）
     * 5. 号源上限校验（每时段最多 10 人，排除已取消）
     * 6. 落库
     */
    private Long doCreateAppointment(Long patientId, CreateAppointmentRequest req) {
        // 0. 校验预约时段：格式 HH:mm-HH:mm，且时间差不超过 30 分钟
        validateTimeSlot(req.getAppointmentTime());

        // 1. 校验医生
        Doctors doctor = doctorsMapper.selectById(req.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        if (doctor.getStatus() == null || doctor.getStatus() != 1) {
            throw new BusinessException("该医生当前不可预约");
        }

        // 2. 校验日期不能是过去
        if (req.getAppointmentDate() == null || req.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessException("预约日期不能早于今天");
        }

        // 3. 重复预约校验（同患者+同医生+同日期+同时段，且状态非已取消）
        long duplicate = this.count(new LambdaQueryWrapper<Appointments>()
                .eq(Appointments::getPatientId, patientId)
                .eq(Appointments::getDoctorId, req.getDoctorId())
                .eq(Appointments::getAppointmentDate, req.getAppointmentDate())
                .eq(Appointments::getAppointmentTime, req.getAppointmentTime())
                .ne(Appointments::getStatus, 2));   // 排除已取消
        if (duplicate > 0) {
            throw new BusinessException("您已预约该医生此时段，请勿重复预约");
        }

        // 4. 号源上限校验：同一医生+日期+时段，已预约数（排除已取消）< 10
        long booked = this.count(new LambdaQueryWrapper<Appointments>()
                .eq(Appointments::getDoctorId, req.getDoctorId())
                .eq(Appointments::getAppointmentDate, req.getAppointmentDate())
                .eq(Appointments::getAppointmentTime, req.getAppointmentTime())
                .ne(Appointments::getStatus, 2));
        if (booked >= 10) {
            throw new BusinessException("该时段号源已约满，请选择其他时段");
        }

        // 5. 落库
        Appointments appt = new Appointments();
        BeanUtils.copyProperties(req, appt);
        appt.setPatientId(patientId);
        appt.setStatus(1);   // 直接已确认（已预约），不需要二次确认流程
        appt.setCreatedAt(LocalDateTime.now());
        appt.setUpdatedAt(LocalDateTime.now());
        this.save(appt);

        log.info("创建预约成功: patientId={}, appointmentId={}, doctorId={}, date={}",
                patientId, appt.getId(), req.getDoctorId(), req.getAppointmentDate());
        return appt.getId();
    }

    /**
     * 分页查询当前用户的预约列表
     * 1. 按 patient_id + 可选 status 分页查询
     * 2. 批量查询关联医生信息，避免 N+1
     * 3. 内存分组组装 DTO
     */
    @Override
    public PageResult<AppointmentDTO> getMyAppointments(Long patientId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Appointments> page = new Page<>(
                pageNum == null ? 1 : pageNum,
                pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Appointments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointments::getPatientId, patientId);
        // 前端"已预约"Tab 传 status=1，为兼容历史 status=0（待确认）数据，同时查询 0 和 1
        if (status != null) {
            if (status == 1) {
                wrapper.in(Appointments::getStatus, 0, 1);
            } else {
                wrapper.eq(Appointments::getStatus, status);
            }
        }
        wrapper.orderByDesc(Appointments::getAppointmentDate)
                .orderByDesc(Appointments::getAppointmentTime);
        IPage<Appointments> result = this.page(page, wrapper);

        List<Appointments> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(
                    Collections.emptyList(),
                    result.getTotal(),
                    result.getCurrent(),
                    result.getSize(),
                    result.getPages());
        }

        // 批量查询医生信息（去重 doctorId）
        Set<Long> doctorIds = records.stream()
                .map(Appointments::getDoctorId)
                .collect(Collectors.toSet());
        List<Doctors> doctors = doctorsMapper.selectBatchIds(doctorIds);
        Map<Long, Doctors> doctorMap = doctors.stream()
                .collect(Collectors.toMap(Doctors::getId, d -> d));

        // 组装 DTO
        List<AppointmentDTO> dtoList = records.stream()
                .map(appt -> convertToDTO(appt, doctorMap.get(appt.getDoctorId())))
                .collect(Collectors.toList());

        return new PageResult<>(
                dtoList,
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getPages());
    }

    /**
     * 取消预约
     * 1. 查询预约是否存在
     * 2. 校验归属（必须是当前登录用户的预约）
     * 3. 校验状态：已取消(2)/已完成(3) 不允许再取消
     * 4. 更新状态为 2 已取消，写入 cancel_reason
     */
    @Override
    @Transactional
    public void cancelAppointment(Long patientId, Long appointmentId, CancelAppointmentRequest req) {
        // 1. 查询预约
        Appointments appt = this.getById(appointmentId);
        if (appt == null) {
            throw new BusinessException(404, "预约不存在");
        }

        // 2. 校验归属
        if (!appt.getPatientId().equals(patientId)) {
            throw new BusinessException(403, "无权取消他人的预约");
        }

        // 3. 校验状态
        Integer currentStatus = appt.getStatus();
        if (currentStatus != null && currentStatus == 2) {
            throw new BusinessException("该预约已取消，请勿重复操作");
        }
        if (currentStatus != null && currentStatus == 3) {
            throw new BusinessException("该预约已完成，无法取消");
        }

        // 4. 更新
        appt.setStatus(2);
        appt.setCancelReason(req.getCancelReason());
        appt.setUpdatedAt(LocalDateTime.now());
        this.updateById(appt);

        log.info("取消预约成功: patientId={}, appointmentId={}, reason={}",
                patientId, appointmentId, req.getCancelReason());
    }

    /**
     * PO + 关联医生 → DTO 转换
     * 显式判空防止 BeanUtils.copyProperties 抛 IllegalArgumentException
     */
    private AppointmentDTO convertToDTO(Appointments appt, Doctors doctor) {
        AppointmentDTO dto = new AppointmentDTO();
        if (appt == null) {
            return dto;
        }
        BeanUtils.copyProperties(appt, dto);
        if (doctor != null) {
            dto.setDoctorName(doctor.getName());
            dto.setDepartment(doctor.getDepartment());
        }
        return dto;
    }

    /** 时段校验器：HH:mm-HH:mm 格式 + 起止时间差不超过 30 分钟 */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String TIME_SLOT_REGEX = "^\\d{2}:\\d{2}-\\d{2}:\\d{2}$";

    private void validateTimeSlot(String appointmentTime) {
        if (appointmentTime == null || !appointmentTime.matches(TIME_SLOT_REGEX)) {
            throw new BusinessException("预约时段格式必须为 HH:mm-HH:mm，例如 09:00-09:30");
        }
        String[] parts = appointmentTime.split("-");
        try {
            LocalTime start = LocalTime.parse(parts[0], TIME_FMT);
            LocalTime end = LocalTime.parse(parts[1], TIME_FMT);
            long minutes = Duration.between(start, end).toMinutes();
            if (minutes <= 0) {
                throw new BusinessException("预约时段结束时间必须晚于开始时间");
            }
            if (minutes > 30) {
                throw new BusinessException("预约时段跨度不能超过 30 分钟，当前为 " + minutes + " 分钟");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("预约时段时间格式不正确：" + e.getMessage());
        }
    }
}

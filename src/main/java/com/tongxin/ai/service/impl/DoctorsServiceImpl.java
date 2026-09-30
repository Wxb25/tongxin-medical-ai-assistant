package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.DoctorDTO;
import com.tongxin.ai.dto.DoctorScheduleDTO;
import com.tongxin.ai.entity.po.Appointments;
import com.tongxin.ai.entity.po.Doctors;
import com.tongxin.ai.mapper.AppointmentsMapper;
import com.tongxin.ai.mapper.DoctorsMapper;
import com.tongxin.ai.service.IDoctorsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DoctorsServiceImpl extends ServiceImpl<DoctorsMapper, Doctors> implements IDoctorsService {

    private final AppointmentsMapper appointmentsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 医生详情缓存 TTL（秒） */
    @Value("${cache.doctor-detail-ttl:600}")
    private long doctorDetailTtl;

    /** 医生排班缓存 TTL（秒），号源实时性要求高，设较短 */
    @Value("${cache.doctor-schedule-ttl:120}")
    private long doctorScheduleTtl;

    /**
     * 分页查询 + 科室/关键词过滤
     * @param department
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<DoctorDTO> searchDoctors(String department, String keyword, Integer pageNum, Integer pageSize) {
        Page<Doctors> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Doctors> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(department), Doctors::getDepartment, department)
                .and(StringUtils.hasText(keyword),
                        w -> w.like(Doctors::getName, keyword)
                                .or().like(Doctors::getSpecialty, keyword)
                                .or().like(Doctors::getIntroduction, keyword))
                .eq(Doctors::getStatus,1)
                .orderByDesc(Doctors::getCreatedAt);
        IPage<Doctors> result = this.page(page, wrapper);
        return convertToPageResult(result);
    }

    /**
     * 根据医生id查询对应医生详情（带 Redis 缓存）
     * 缓存 key: cache:doctor:detail:{id}，TTL 由 cache.doctor-detail-ttl 配置
     */
    @Override
    public DoctorDTO getDoctorDetail(Long doctorId) {
        String cacheKey = "cache:doctor:detail:" + doctorId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof DoctorDTO dto) {
                return dto;
            }
        } catch (Exception e) {
            log.warn("医生详情缓存读取失败，降级查库: doctorId={}", doctorId, e);
        }

        Doctors doctor = this.getById(doctorId);
        if (doctor == null) return null;
        DoctorDTO dto = new DoctorDTO();
        BeanUtils.copyProperties(doctor, dto);

        try {
            redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofSeconds(doctorDetailTtl));
        } catch (Exception e) {
            log.warn("医生详情缓存写入失败: doctorId={}", doctorId, e);
        }
        return dto;
    }

    /** 每时段号源上限 */
    private static final int SLOT_TOTAL = 10;
    /** 规则时段：上午 08:00-12:00，下午 14:00-17:30，每 30 分钟一段 */
    private static final List<String> RULED_SLOTS = buildRuledSlots();

    /**
     * 获取医生排班（含剩余号源计算，带 Redis 缓存）
     * 缓存 key: cache:doctor:schedule:{id}:{date|next7days}，TTL 由 cache.doctor-schedule-ttl 配置
     * 规则：只要医生 status=1（在岗），按固定规则生成时段
     *   上午 08:00-12:00 共 8 段；下午 14:00-17:30 共 7 段；每段上限 10
     * remaining = SLOT_TOTAL - 已预约数（已取消 status=2 不计）
     *
     * @param doctorId 医生ID
     * @param date     可选日期，传了只算这一天；否则未来 7 天
     * @return 排班列表
     */
    @Override
    public List<DoctorScheduleDTO> getDoctorSchedule(Long doctorId, LocalDate date) {
        String dateKey = date != null ? date.toString() : "next7days";
        String cacheKey = "cache:doctor:schedule:" + doctorId + ":" + dateKey;

        // 1. 查缓存
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof DoctorScheduleDTO) {
                return (List<DoctorScheduleDTO>) list;
            }
        } catch (Exception e) {
            log.warn("医生排班缓存读取失败，降级查库: doctorId={}", doctorId, e);
        }

        // 2. 查库计算
        List<DoctorScheduleDTO> result = doGetDoctorSchedule(doctorId, date);

        // 3. 写缓存（仅非空结果，空结果不缓存避免医生刚上岗时缓存空值）
        if (result != null && !result.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(cacheKey, result, Duration.ofSeconds(doctorScheduleTtl));
            } catch (Exception e) {
                log.warn("医生排班缓存写入失败: doctorId={}", doctorId, e);
            }
        }
        return result;
    }

    /** 排班计算核心逻辑（不含缓存） */
    private List<DoctorScheduleDTO> doGetDoctorSchedule(Long doctorId, LocalDate date) {
        // 1. 查询医生是否存在
        Doctors doctor = this.getById(doctorId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }

        // 2. 医生不在岗直接返回空（status != 1）
        if (doctor.getStatus() == null || doctor.getStatus() != 1) {
            return Collections.emptyList();
        }

        // 3. 确定要返回的日期列表
        List<LocalDate> targetDates = new ArrayList<>();
        if (date != null) {
            targetDates.add(date);
        } else {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                targetDates.add(today.plusDays(i));
            }
        }

        // 4. 一次性查该医生这些日期的有效预约
        LambdaQueryWrapper<Appointments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointments::getDoctorId, doctorId)
                .in(Appointments::getStatus, 0, 1, 3)   // 排除已取消（status=2）
                .in(Appointments::getAppointmentDate, targetDates);
        List<Appointments> appts = appointmentsMapper.selectList(wrapper);

        // 按 date + timeSlot 分组计数
        Map<String, Long> bookedMap = appts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDate() + "#" + a.getAppointmentTime(),
                        Collectors.counting()));

        // 5. 组装 DTO：每个目标日期 × 每个规则时段
        List<DoctorScheduleDTO> result = new ArrayList<>();
        for (LocalDate d : targetDates) {
            for (String slot : RULED_SLOTS) {
                String key = d + "#" + slot;
                int booked = bookedMap.getOrDefault(key, 0L).intValue();
                int remaining = Math.max(0, SLOT_TOTAL - booked);

                DoctorScheduleDTO dto = new DoctorScheduleDTO();
                dto.setDate(d);
                dto.setTimeSlot(slot);
                dto.setTotal(SLOT_TOTAL);
                dto.setBooked(booked);
                dto.setRemaining(remaining);
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 按规则生成时段列表：上午 08:00-12:00 + 下午 14:00-17:30，每 30 分钟一段
     * 格式 HH:mm-HH:mm，与 CreateAppointmentRequest 的 @Pattern 一致
     */
    private static List<String> buildRuledSlots() {
        List<String> slots = new ArrayList<>();
        // 上午 08:00 - 12:00
        slots.addAll(generateSlots(8, 0, 12, 0));
        // 下午 14:00 - 17:30
        slots.addAll(generateSlots(14, 0, 17, 30));
        return Collections.unmodifiableList(slots);
    }

    private static List<String> generateSlots(int startH, int startM, int endH, int endM) {
        List<String> slots = new ArrayList<>();
        int curH = startH, curM = startM;
        while (true) {
            int nextH = curH, nextM = curM + 30;
            if (nextM >= 60) { nextH += 1; nextM -= 60; }
            // 超过结束时间则停止
            if (nextH > endH || (nextH == endH && nextM > endM)) break;
            slots.add(String.format("%02d:%02d-%02d:%02d", curH, curM, nextH, nextM));
            curH = nextH; curM = nextM;
        }
        return slots;
    }

    /**
     * 转换类型
     * @param page
     * @return
     */
    private PageResult<DoctorDTO> convertToPageResult(IPage<Doctors> page) {
        List<DoctorDTO> records = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream()
                .map(doctor -> {
                    DoctorDTO dto = new DoctorDTO();
                    BeanUtils.copyProperties(doctor, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }
}

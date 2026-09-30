package com.tongxin.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.dto.AdminAppointmentDTO;
import com.tongxin.ai.entity.po.Appointments;
import com.tongxin.ai.entity.po.Doctors;
import com.tongxin.ai.entity.po.Drugs;
import com.tongxin.ai.entity.po.Users;
import com.tongxin.ai.service.IAppointmentsService;
import com.tongxin.ai.service.IDoctorsService;
import com.tongxin.ai.service.IDrugsService;
import com.tongxin.ai.service.IUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员管理控制器
 * 提供医生与用户账号的管理功能（CRUD、启用/禁用）
 * 所有接口需管理员 token（由 AdminAuthInterceptor 校验）
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminManageController {

    private final IDoctorsService doctorsService;
    private final IUsersService usersService;
    private final IAppointmentsService appointmentsService;
    private final IDrugsService drugsService;

    // ==================== 医生管理 ====================

    /**
     * 医生列表（分页，支持科室/关键词筛选）
     */
    @GetMapping("/doctors")
    public Result<PageResult<Doctors>> listDoctors(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Doctors> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Doctors> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(department), Doctors::getDepartment, department)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Doctors::getName, keyword)
                        .or().like(Doctors::getSpecialty, keyword))
                .orderByDesc(Doctors::getId);
        IPage<Doctors> result = doctorsService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages()));
    }

    /**
     * 新增医生
     */
    @PostMapping("/doctors")
    public Result<Long> addDoctor(@RequestBody Doctors doctor) {
        doctor.setId(null);
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.setUpdatedAt(LocalDateTime.now());
        if (doctor.getStatus() == null) doctor.setStatus(1);
        doctorsService.save(doctor);
        return Result.success(doctor.getId());
    }

    /**
     * 编辑医生
     */
    @PutMapping("/doctors/{id}")
    public Result<Void> updateDoctor(@PathVariable Long id, @RequestBody Doctors doctor) {
        Doctors existing = doctorsService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctor.setId(id);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorsService.updateById(doctor);
        return Result.success();
    }

    /**
     * 删除医生
     */
    @DeleteMapping("/doctors/{id}")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        doctorsService.removeById(id);
        return Result.success();
    }

    /**
     * 启用/禁用医生
     */
    @PutMapping("/doctors/{id}/status")
    public Result<Void> updateDoctorStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Doctors doctor = new Doctors();
        doctor.setId(id);
        doctor.setStatus(body.get("status"));
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorsService.updateById(doctor);
        return Result.success();
    }

    // ==================== 用户管理 ====================

    /**
     * 用户列表（分页，支持关键词搜索用户名/姓名/手机号）
     */
    @GetMapping("/users")
    public Result<PageResult<Users>> listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Users> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
                        .like(Users::getUsername, keyword)
                        .or().like(Users::getRealName, keyword)
                        .or().like(Users::getPhone, keyword))
                .eq(Users::getDeleted, 0)
                .orderByDesc(Users::getId);
        IPage<Users> result = usersService.page(page, wrapper);
        // 脱敏：不返回密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages()));
    }

    /**
     * 编辑用户信息（用户名不可改，密码可重置）
     */
    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Users user) {
        Users existing = usersService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setId(id);
        user.setUsername(existing.getUsername()); // 用户名不可改
        user.setUpdatedAt(LocalDateTime.now());
        usersService.updateById(user);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Users user = new Users();
        user.setId(id);
        user.setStatus(body.get("status"));
        user.setUpdatedAt(LocalDateTime.now());
        usersService.updateById(user);
        return Result.success();
    }

    // ==================== 挂号管理 ====================

    /**
     * 挂号记录列表（支持科室/医生/日期/时段/状态筛选）
     */
    @GetMapping("/appointments")
    public Result<PageResult<AdminAppointmentDTO>> listAppointments(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "timeSlot", required = false) String timeSlot,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        // 1. 若按科室筛选，先查出该科室下所有医生ID
        List<Long> doctorIds = null;
        if (StringUtils.hasText(department)) {
            doctorIds = doctorsService.list(new LambdaQueryWrapper<Doctors>()
                            .eq(Doctors::getDepartment, department))
                    .stream().map(Doctors::getId).collect(Collectors.toList());
            if (doctorIds.isEmpty()) {
                return Result.success(new PageResult<>(Collections.emptyList(), 0L, pageNum.longValue(), pageSize.longValue(), 0L));
            }
        }

        // 2. 查预约
        Page<Appointments> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointments> wrapper = new LambdaQueryWrapper<>();
        if (doctorId != null) wrapper.eq(Appointments::getDoctorId, doctorId);
        if (doctorIds != null) wrapper.in(Appointments::getDoctorId, doctorIds);
        if (StringUtils.hasText(date)) wrapper.eq(Appointments::getAppointmentDate, LocalDate.parse(date));
        if (StringUtils.hasText(timeSlot)) wrapper.eq(Appointments::getAppointmentTime, timeSlot);
        if (status != null) wrapper.eq(Appointments::getStatus, status);
        wrapper.orderByDesc(Appointments::getAppointmentDate).orderByDesc(Appointments::getId);
        IPage<Appointments> result = appointmentsService.page(page, wrapper);

        // 3. 批量查医生和患者，组装 DTO（避免 N+1）
        Set<Long> dIds = result.getRecords().stream().map(Appointments::getDoctorId).collect(Collectors.toSet());
        Set<Long> pIds = result.getRecords().stream().map(Appointments::getPatientId).collect(Collectors.toSet());
        Map<Long, Doctors> doctorMap = dIds.isEmpty() ? Collections.emptyMap() :
                doctorsService.listByIds(dIds).stream().collect(Collectors.toMap(Doctors::getId, d -> d));
        Map<Long, Users> userMap = pIds.isEmpty() ? Collections.emptyMap() :
                usersService.listByIds(pIds).stream().collect(Collectors.toMap(Users::getId, u -> u));

        List<AdminAppointmentDTO> dtoList = result.getRecords().stream().map(ap -> {
            AdminAppointmentDTO dto = new AdminAppointmentDTO();
            BeanUtils.copyProperties(ap, dto);
            Doctors d = doctorMap.get(ap.getDoctorId());
            if (d != null) {
                dto.setDoctorName(d.getName());
                dto.setDepartment(d.getDepartment());
            }
            Users u = userMap.get(ap.getPatientId());
            if (u != null) {
                dto.setPatientName(u.getRealName());
                dto.setPatientPhone(u.getPhone());
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(dtoList, result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages()));
    }

    /**
     * 编辑挂号记录（可修改日期、时段、症状、状态、取消原因）
     */
    @PutMapping("/appointments/{id}")
    public Result<Void> updateAppointment(@PathVariable Long id, @RequestBody Appointments appointment) {
        Appointments existing = appointmentsService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        appointment.setId(id);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentsService.updateById(appointment);
        return Result.success();
    }

    // ==================== 药品管理 ====================

    /**
     * 药品列表（分页，支持分类/关键词筛选）
     */
    @GetMapping("/drugs")
    public Result<PageResult<Drugs>> listDrugs(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<Drugs> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Drugs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), Drugs::getCategory, category)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Drugs::getName, keyword)
                        .or().like(Drugs::getGenericName, keyword))
                .orderByDesc(Drugs::getId);
        IPage<Drugs> result = drugsService.page(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages()));
    }

    /**
     * 新增药品
     */
    @PostMapping("/drugs")
    public Result<Long> addDrug(@RequestBody Drugs drug) {
        drug.setId(null);
        drug.setCreatedAt(LocalDateTime.now());
        drug.setUpdatedAt(LocalDateTime.now());
        if (drug.getStatus() == null) drug.setStatus(1);
        drugsService.save(drug);
        return Result.success(drug.getId());
    }

    /**
     * 编辑药品
     */
    @PutMapping("/drugs/{id}")
    public Result<Void> updateDrug(@PathVariable Long id, @RequestBody Drugs drug) {
        Drugs existing = drugsService.getById(id);
        if (existing == null) {
            throw new BusinessException(404, "药品不存在");
        }
        drug.setId(id);
        drug.setUpdatedAt(LocalDateTime.now());
        drugsService.updateById(drug);
        return Result.success();
    }

    /**
     * 删除药品
     */
    @DeleteMapping("/drugs/{id}")
    public Result<Void> deleteDrug(@PathVariable Long id) {
        drugsService.removeById(id);
        return Result.success();
    }

    /**
     * 上架/下架药品
     */
    @PutMapping("/drugs/{id}/status")
    public Result<Void> updateDrugStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Drugs drug = new Drugs();
        drug.setId(id);
        drug.setStatus(body.get("status"));
        drug.setUpdatedAt(LocalDateTime.now());
        drugsService.updateById(drug);
        return Result.success();
    }
}

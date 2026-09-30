package com.tongxin.ai.controller;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.dto.DoctorDTO;
import com.tongxin.ai.dto.DoctorScheduleDTO;
import com.tongxin.ai.service.IDoctorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private  final IDoctorsService doctorsService;

    @GetMapping
    public Result<PageResult<DoctorDTO>> searchDoctors(
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<DoctorDTO> page = doctorsService.searchDoctors(department, keyword, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据id搜索对应医生详情
     * @param doctorId 医生id
     * @return
     */
    @GetMapping("/{doctorId}")
    public Result<DoctorDTO> getDoctorDetail(@PathVariable Long doctorId){
        DoctorDTO doctor = doctorsService.getDoctorDetail(doctorId);
        if (doctor == null)return  Result.error("医生不存在");
        return Result.success(doctor);
    }

    /**
     * 获取医生排班
     * GET /api/doctors/{doctorId}/schedule
     *
     * @param doctorId 医生ID
     * @param date     可选日期（格式 yyyy-MM-dd），不传则返回未来 7 天排班
     * @return 排班列表（含剩余号源）
     */
    @GetMapping("/{doctorId}/schedule")
    public Result<List<DoctorScheduleDTO>> getDoctorSchedule(
            @PathVariable("doctorId") Long doctorId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DoctorScheduleDTO> list = doctorsService.getDoctorSchedule(doctorId, date);
        return Result.success(list);
    }

}

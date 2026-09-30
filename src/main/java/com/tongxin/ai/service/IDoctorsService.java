package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.DoctorDTO;
import com.tongxin.ai.dto.DoctorScheduleDTO;
import com.tongxin.ai.entity.po.Doctors;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IDoctorsService extends IService<Doctors> {

    PageResult<DoctorDTO> searchDoctors(String department, String keyword, Integer pageNum, Integer pageSize);

    DoctorDTO getDoctorDetail(Long doctorId);

    List<DoctorScheduleDTO> getDoctorSchedule(Long doctorId, LocalDate date);
}

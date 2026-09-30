package com.tongxin.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 医生响应 DTO
 * 剔除运维字段（status/created_at/updated_at），仅保留前端需要的业务字段
 *
 * @author wyq
 */
@Data
public class DoctorDTO {

    /** 主键ID */
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 医生姓名 */
    private String name;

    /** 所属科室 */
    private String department;

    /** 职称 */
    private String title;

    /** 擅长领域 */
    private String specialty;

    /** 个人简介 */
    private String introduction;

    /** 排班信息（JSON格式） */
    private String schedule;

    /** 问诊费用 */
    private BigDecimal consultationFee;
}

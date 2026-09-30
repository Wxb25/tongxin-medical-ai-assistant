package com.tongxin.ai.entity.po;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("doctors")
public class Doctors implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    @TableField("user_id")
    private Long userId;

    /** 医生姓名 */
    @TableField("name")
    private String name;

    /** 所属科室 */
    @TableField("department")
    private String department;

    /** 职称 */
    @TableField("title")
    private String title;

    /** 擅长领域 */
    @TableField("specialty")
    private String specialty;

    /** 个人简介 */
    @TableField("introduction")
    private String introduction;

    /** 排班信息（JSON格式） */
    @TableField("schedule")
    private String schedule;

    /** 问诊费用 */
    @TableField("consultation_fee")
    private BigDecimal consultationFee;

    /** 状态：0禁用/1启用 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}

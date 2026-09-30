package com.tongxin.ai.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
@TableName("appointments")
public class Appointments implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 患者ID */
    @TableField("patient_id")
    private Long patientId;

    /** 医生ID */
    @TableField("doctor_id")
    private Long doctorId;

    /** 预约日期 */
    @TableField("appointment_date")
    private LocalDate appointmentDate;

    /** 预约时段 */
    @TableField("appointment_time")
    private String appointmentTime;

    /** 症状描述 */
    @TableField("symptom")
    private String symptom;

    /** 预约状态：0待确认/1已确认/2已取消/3已完成 */
    @TableField("status")
    private Integer status;

    /** 取消原因 */
    @TableField("cancel_reason")
    private String cancelReason;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}

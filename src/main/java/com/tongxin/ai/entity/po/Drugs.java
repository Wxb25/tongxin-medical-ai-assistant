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
@TableName("drugs")
public class Drugs implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 药品名称 */
    @TableField("name")
    private String name;

    /** 通用名 */
    @TableField("generic_name")
    private String genericName;

    /** 药品分类 */
    @TableField("category")
    private String category;

    /** 规格 */
    @TableField("specification")
    private String specification;

    /** 单位 */
    @TableField("unit")
    private String unit;

    /** 生产厂家 */
    @TableField("manufacturer")
    private String manufacturer;

    /** 价格 */
    @TableField("price")
    private BigDecimal price;

    /** 库存数量 */
    @TableField("stock")
    private Integer stock;

    /** 用药说明 */
    @TableField("instruction")
    private String instruction;

    /** 副作用 */
    @TableField("side_effects")
    private String sideEffects;

    /** 状态：0下架/1上架 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;


}

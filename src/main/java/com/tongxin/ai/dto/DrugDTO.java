package com.tongxin.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 药品响应 DTO
 * 剔除运维字段（status/created_at/updated_at），仅保留前端需要的业务字段
 *
 * @author wyq
 */
@Data
public class DrugDTO {

    /** 主键ID */
    private Long id;

    /** 药品名称 */
    private String name;

    /** 通用名 */
    private String genericName;

    /** 药品分类 */
    private String category;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 生产厂家 */
    private String manufacturer;

    /** 价格 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 用药说明 */
    private String instruction;

    /** 副作用 */
    private String sideEffects;
}

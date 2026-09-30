package com.tongxin.ai.service;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.DrugDTO;
import com.tongxin.ai.entity.po.Drugs;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface IDrugsService extends IService<Drugs> {

    /**
     * 药品搜索（简单 LIKE 模糊查询）
     *
     * @param keyword  关键词（必填，匹配药品名称/通用名/生产厂家）
     * @param category 分类（可选）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果（DTO 形式，剔除运维字段）
     */
    PageResult<DrugDTO> searchDrugs(String keyword, String category, Integer pageNum, Integer pageSize);

    /**
     * 根据id搜索对应药品
     * @param drugId 药品id
     * @return
     */
    DrugDTO getDrugDetail(Long drugId);
}

package com.tongxin.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.dto.DrugDTO;
import com.tongxin.ai.entity.po.Drugs;
import com.tongxin.ai.mapper.DrugsMapper;
import com.tongxin.ai.service.IDrugsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wyq
 * @since 2026-09-02
 */
@Service
public class DrugsServiceImpl extends ServiceImpl<DrugsMapper, Drugs> implements IDrugsService {

    /**
     * 搜索药品
     * @param keyword  关键词（必填，匹配药品名称/通用名/生产厂家）
     * @param category 分类（可选）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public PageResult<DrugDTO> searchDrugs(String keyword, String category, Integer pageNum, Integer pageSize) {
        Page<Drugs> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Drugs> wrapper = new LambdaQueryWrapper<>();
        // 简单 LIKE %keyword% 模糊查询：匹配药品名称/通用名/生产厂家
        wrapper.and(StringUtils.hasText(keyword),
                w -> w.like(Drugs::getName, keyword)
                        .or().like(Drugs::getGenericName, keyword)
                        .or().like(Drugs::getManufacturer, keyword))
                .eq(StringUtils.hasText(category), Drugs::getCategory, category)
                // 仅查上架药品
                .eq(Drugs::getStatus, 1)
                .orderByDesc(Drugs::getCreatedAt);
        IPage<Drugs> result = this.page(page, wrapper);
        return convertToPageResult(result);
    }

    /**
     * 根据药品id查询对应药品详情
     * @param drugId 药品id
     * @return
     */
    @Override
    public DrugDTO getDrugDetail(Long drugId) {
        Drugs drug = this.getById(drugId);
        if(drug == null)return null;
        DrugDTO dto = new DrugDTO();
        BeanUtils.copyProperties(drug, dto);
        return dto;
    }

    /**
     * 将 IPage&lt;Drugs&gt; 转为 PageResult&lt;DrugDTO&gt;
     * 通过 BeanUtils 拷贝同名属性完成实体 -> DTO 映射，剔除运维字段
     */
    private PageResult<DrugDTO> convertToPageResult(IPage<Drugs> page) {
        List<DrugDTO> records = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream()
                        .map(drug -> {
                            DrugDTO dto = new DrugDTO();
                            BeanUtils.copyProperties(drug, dto);
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

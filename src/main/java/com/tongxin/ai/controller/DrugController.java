package com.tongxin.ai.controller;

import com.tongxin.ai.common.PageResult;
import com.tongxin.ai.common.Result;
import com.tongxin.ai.dto.DrugDTO;
import com.tongxin.ai.service.IDrugsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 药品管理控制器
 *
 * @author wyq
 */
@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final IDrugsService drugsService;

    /**
     * 搜索药品
     * GET /api/drugs/search
     *
     * @param keyword  关键词（必填）
     * @param category 分类（可选）
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     */
    @GetMapping("/search")
    public Result<PageResult<DrugDTO>> searchDrugs(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult<DrugDTO> page = drugsService.searchDrugs(keyword, category, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据id搜索对应药品详情
     * @param drugId 药品id
     * @return
     */
    @GetMapping("/{drugId}")
    public Result<DrugDTO> getDrugDetail(@PathVariable Long drugId){
        DrugDTO drug = drugsService.getDrugDetail(drugId);
        if (drug == null)return  Result.error("药品不存在");
        return Result.success(drug);
    }
}


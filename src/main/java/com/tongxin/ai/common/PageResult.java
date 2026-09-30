package com.tongxin.ai.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果包装类
 * 用于对外屏蔽 MyBatis-Plus 的 IPage 内部字段，提供稳定的分页契约
 *
 * @author wyq
 */
@Data
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /** 总页数 */
    private Long pages;

    public PageResult() {
        this.records = Collections.emptyList();
        this.total = 0L;
        this.current = 1L;
        this.size = 10L;
        this.pages = 0L;
    }

    public PageResult(List<T> records, Long total, Long current, Long size, Long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }

    /**
     * 元素类型转换：将 PageResult&lt;S&gt; 转为 PageResult&lt;T&gt;
     * 用于实体 -> DTO 的映射场景
     *
     * @param mapper 映射函数
     */
    public <S> PageResult<T> from(PageResult<S> source, Function<S, T> mapper) {
        List<T> mapped = source.getRecords().stream()
                .map(mapper)
                .collect(Collectors.toList());
        return new PageResult<>(mapped, source.getTotal(), source.getCurrent(), source.getSize(), source.getPages());
    }
}

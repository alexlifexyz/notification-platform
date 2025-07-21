package com.enterprise.notification.admin.dto.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageResult() {}

    public PageResult(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        this.pages = (total + size - 1) / size;
    }
}

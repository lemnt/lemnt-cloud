package com.lemnt.common.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用分页结果
 *
 * @param <T> 记录类型
 * @author wangxx
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页记录
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    public PageResult() {
        this.records = new ArrayList<>();
        this.total = 0;
    }

    public PageResult(List<T> records, long total) {
        this.records = records != null ? records : new ArrayList<>();
        this.total = total;
    }

    public static <T> PageResult<T> of(List<T> records, long total) {
        return new PageResult<>(records, total);
    }
}

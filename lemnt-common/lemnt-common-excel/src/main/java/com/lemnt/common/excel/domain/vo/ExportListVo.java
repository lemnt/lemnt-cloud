package com.lemnt.common.excel.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 导出结果 List
 *
 * @param <T>
 * @author wangxx
 */
@Data
public class ExportListVo<T> {

    /**
     * 总数
     */
    private long total;

    /**
     * 列表
     */
    private List<T> list;
}

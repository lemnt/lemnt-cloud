package com.lemnt.common.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用分页查询参数
 *
 * @author wangxx
 */
@Data
public class PageQry implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码（默认 1）
     */
    private int pageNum = 1;

    /**
     * 每页条数（默认 10）
     */
    private int pageSize = 10;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    public PageQry() {
    }

    public PageQry(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}

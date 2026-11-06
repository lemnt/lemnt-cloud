package com.lemnt.product.application.product.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规格项分页查询。
 */
public record ProductSpecPageQry(
    String name,
    int pageNum,
    int pageSize,
    LocalDateTime createTimeStart,
    LocalDateTime createTimeEnd)
    implements Serializable {
}

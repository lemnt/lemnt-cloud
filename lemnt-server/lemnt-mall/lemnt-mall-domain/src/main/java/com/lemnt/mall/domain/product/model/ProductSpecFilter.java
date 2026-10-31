package com.lemnt.mall.domain.product.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 规格管理查询条件。 */
public record ProductSpecFilter(
        String name,
        Long specId,
        int pageNum,
        int pageSize,
        LocalDateTime createTimeStart,
        LocalDateTime createTimeEnd
) implements Serializable {
}

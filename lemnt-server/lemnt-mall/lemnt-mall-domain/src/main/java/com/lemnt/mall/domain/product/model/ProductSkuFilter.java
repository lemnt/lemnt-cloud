package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** SKU管理分页条件。 */
public record ProductSkuFilter(
        Long spuId,
        String code,
        String name,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

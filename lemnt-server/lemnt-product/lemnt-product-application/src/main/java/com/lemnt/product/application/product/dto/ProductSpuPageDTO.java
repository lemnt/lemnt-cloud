package com.lemnt.product.application.product.dto;

import com.lemnt.product.domain.product.model.ProductSkuTotals;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 商品SPU分页数据传输对象。
 */
public record ProductSpuPageDTO(
        List<ProductSpuDTO> records, long total, Map<Long, ProductSkuTotals> skuTotals)
        implements Serializable {
    /** 导出和已有分页调用可继续使用原分页结果。 */
    public ProductSpuPageDTO(List<ProductSpuDTO> records, long total) {
        this(records, total, Map.of());
    }
}

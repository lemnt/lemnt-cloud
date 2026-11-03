package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 门店商品SKU数据传输对象。
 */
public record StoreSkuDTO(
        Long id, String name, String price, int stock, String image, List<Spec> specs)
        implements Serializable {
    /** 编号与名称保持一一对应。 */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}

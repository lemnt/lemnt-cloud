package com.lemnt.product.application.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 门店商品SKU数据传输对象。
 */
public record StoreSkuDTO(
        Long id,
        String name,
        String price,
        int stock,
        String image,
        List<Spec> specs,
        BigDecimal weight,
        BigDecimal volume,
        String marketPrice)
        implements Serializable {
    /** 旧调用没有商品补充资料，保留未知值。 */
    public StoreSkuDTO(
            Long id,
            String name,
            String price,
            int stock,
            String image,
            List<Spec> specs,
            BigDecimal weight,
            BigDecimal volume) {
        this(id, name, price, stock, image, specs, weight, volume, null);
    }

    /** 历史调用未携带配送参数，不能补造为零。 */
    public StoreSkuDTO(
            Long id, String name, String price, int stock, String image, List<Spec> specs) {
        this(id, name, price, stock, image, specs, null, null);
    }

    /**
     * 编号与名称保持一一对应。
     */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}

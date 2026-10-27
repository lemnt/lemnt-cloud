package com.lemnt.mall.domain.product.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 商品聚合首期模型。
 */
public record Product(
        Long id,
        String productCode,
        String name,
        String description,
        String mainImage,
        BigDecimal price,
        Integer stock,
        String status,
        LocalDateTime createTime
) {
    /**
     * 创建。
     */
    public static Product create(String code, String name, String description, String image,
            BigDecimal price, Integer stock) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("商品编码不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("库存不能小于0");
        }
        return new Product(null, code.trim(), name.trim(), description, image,
                price.setScale(2, RoundingMode.HALF_UP), stock, "OFF_SHELF", null);
    }
}

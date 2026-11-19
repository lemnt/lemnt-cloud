package com.lemnt.product.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 门店商品SKU响应。
 */
@Schema(description = "StoreSku公开响应")
public record StoreSkuRes(
        @Schema(description = "id") Long id,
        @Schema(description = "name") String name,
        @Schema(description = "当前售价（元，精确两位小数字符串）") String price,
        @Schema(description = "当前可用库存，不表示库存预留") int stock,
        @Schema(description = "image") String image,
        @Schema(description = "规格对象列表") List<Spec> specs,
        @Schema(description = "重量（千克），历史未知为null") BigDecimal weight,
        @Schema(description = "体积（立方米），历史未知为null") BigDecimal volume,
        @Schema(description = "划线价，两位小数的人民币元文本，未知为null") String marketPrice)
        implements Serializable {
    /** 旧调用没有商品补充资料，保留未知值。 */
    public StoreSkuRes(
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

    /**
     * 编号与名称保持一一对应。
     */
    @Schema(description = "商品规格")
    public record Spec(
            @Schema(description = "规格项编号") Long specId,
            @Schema(description = "规格名称，写入时可省略") String specName,
            @Schema(description = "规格值编号") Long specValueId,
            @Schema(description = "规格值名称，写入时可省略") String specValueName)
            implements Serializable {}
}

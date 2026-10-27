package com.lemnt.mall.domain.promotion.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 目录计价事实。
 */
public record CatalogPricingFacts(
        long skuId,
        long spuId,
        String productCode,
        String name,
        long unitPriceFen,
        int availableStock
) {
    /**
     * 摘要。
     */
    public String hash() {
        String canonical =
        "MALL_PRICING_V1|"
        + skuId
        + "|"
        + spuId
        + "|"
        + unitPriceFen
        + "|"
        + productCode.getBytes(StandardCharsets.UTF_8).length
        + ":"
        + productCode
        + "|"
        + name.getBytes(StandardCharsets.UTF_8).length
        + ":"
        + name
        + "|SELLABLE";
        try {
            return HexFormat.of()
                .formatHex(
                    MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

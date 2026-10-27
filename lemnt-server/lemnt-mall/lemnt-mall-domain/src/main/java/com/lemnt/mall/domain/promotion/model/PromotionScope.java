package com.lemnt.mall.domain.promotion.model;

import java.util.HashSet;
import java.util.List;

/**
 * 营销范围。
 */
public record PromotionScope(
        Kind kind,
        List<Long> ids
) {
    /**
     * 类型。
     */
    public enum Kind {
        ALL,
        SPU_IDS,
        SKU_IDS
    }

    /**
     * 创建营销范围实例。
     */
    public PromotionScope {
        PricingException.require(kind != null && ids != null);
        PricingException.require(ids.size() <= 1000);
        PricingException.require(kind == Kind.ALL ? ids.isEmpty() : !ids.isEmpty());
        PricingException.require(ids.stream().allMatch(id -> id != null && id > 0));
        PricingException.require(new HashSet<>(ids).size() == ids.size());
        ids = ids.stream().sorted().toList();
    }

    /**
     * 校验密码与已保存凭证是否匹配。
     */
    public boolean matches(PricingLine line) {
        return switch (kind) {
            case ALL -> true;
            case SPU_IDS -> ids.contains(line.spuId());
            case SKU_IDS -> ids.contains(line.productId());
        };
    }
}

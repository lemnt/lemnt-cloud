package com.lemnt.mall.domain.promotion.model;

import java.util.HashSet;
import java.util.List;

/**
 * 营销目录规则。
 */
public final class PromotionCatalogRules {
    private PromotionCatalogRules() {}

    /**
     * 编号集合。
     */
    public static List<Long> ids(List<Long> ids, int maximum) {
        if (ids == null
                || ids.isEmpty()
                || ids.size() > maximum
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Invalid catalog batch");
        }
        return ids.stream().sorted().toList();
    }
}

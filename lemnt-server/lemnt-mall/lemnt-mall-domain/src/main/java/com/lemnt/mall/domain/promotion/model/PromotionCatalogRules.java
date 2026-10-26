package com.lemnt.mall.domain.promotion.model;

import java.util.*;

public final class PromotionCatalogRules {
    private PromotionCatalogRules() {}

    public static List<Long> ids(List<Long> ids, int maximum) {
        if (ids == null
                || ids.isEmpty()
                || ids.size() > maximum
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("Invalid catalog batch");
        return ids.stream().sorted().toList();
    }
}

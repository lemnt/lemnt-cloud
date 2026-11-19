package com.lemnt.product.domain.product.model;

import java.util.List;
import java.util.Set;

/** 商品目录筛选；分类为调用方展开后的并集，排序只允许固定业务字段。 */
public record StoreFilter(
        String name,
        Long categoryId,
        int offset,
        int limit,
        List<Long> categoryIds,
        String sortField,
        boolean sortAsc) {
    public StoreFilter {
        categoryIds = categoryIds == null ? List.of() : List.copyOf(categoryIds);
        if (sortField != null && !Set.of("price", "salesCount", "createTime").contains(sortField)) {
            throw new IllegalArgumentException("排序字段仅支持price、salesCount、createTime");
        }
    }

    /** 兼容既有内部单分类调用，保持默认运营排序。 */
    public StoreFilter(String name, Long categoryId, int offset, int limit) {
        this(name, categoryId, offset, limit, List.of(), null, true);
    }
}

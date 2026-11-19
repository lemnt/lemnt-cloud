package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.StorePageDTO;
import com.lemnt.product.application.product.dto.StoreProductDTO;
import com.lemnt.product.application.product.qry.StorePageQry;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;
import com.lemnt.product.domain.product.model.StoreFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 门店目录查询执行器。
 */
@Component
@RequiredArgsConstructor
public class StoreProductPageQueryExe {
    private final ProductSpuGateway productSpuGateway;
    private final ProductCategoryGateway categoryGateway;

    private String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    /**
     * 执行门店分页查询。
     */
    public StorePageDTO execute(StorePageQry q) {
        if (q == null
                || q.pageNum() < 1
                || q.pageNum() > 1000000
                || q.pageSize() < 1
                || q.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        if (q.categoryId() != null) {
            ProductCatalogValidation.id(q.categoryId());
        }
        String name =
                q.name() == null || q.name().isBlank()
                        ? null
                        : ProductCatalogValidation.text(q.name(), 200, "商品名称");
        var categoryIds = new LinkedHashSet<Long>();
        if (q.categoryId() != null) categoryIds.add(q.categoryId());
        if (q.categoryIds() != null) {
            if (q.categoryIds().size() > 100) throw new IllegalArgumentException("分类最多100个");
            q.categoryIds().forEach(ProductCatalogValidation::id);
            categoryIds.addAll(q.categoryIds());
        }
        // 先校验排序再访问存储；只展开原始条件的启用直接子分类，避免递归或N+1。
        var requested =
                new StoreFilter(
                        name,
                        null,
                        (q.pageNum() - 1) * q.pageSize(),
                        q.pageSize(),
                        List.copyOf(categoryIds),
                        q.sortField(),
                        !Boolean.FALSE.equals(q.sortAsc()));
        if (!categoryIds.isEmpty()) {
            categoryGateway
                    .enabledChildren(List.copyOf(categoryIds))
                    .forEach(v -> categoryIds.add(v.id()));
        }
        var f =
                new StoreFilter(
                        name,
                        null,
                        requested.offset(),
                        requested.limit(),
                        List.copyOf(categoryIds),
                        requested.sortField(),
                        requested.sortAsc());
        return new StorePageDTO(
                productSpuGateway.products(f).stream()
                        .map(
                                v ->
                                        new StoreProductDTO(
                                                v.id(),
                                                v.name(),
                                                v.categoryId(),
                                                v.brandId(),
                                                v.image(),
                                                money(v.minPrice()),
                                                money(v.maxPrice()),
                                                v.available(),
                                                v.salesCount(),
                                                v.displaySalesCount()))
                        .toList(),
                productSpuGateway.storeCount(f));
    }
}

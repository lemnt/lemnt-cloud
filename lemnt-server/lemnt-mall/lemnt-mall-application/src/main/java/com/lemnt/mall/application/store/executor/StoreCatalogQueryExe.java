package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.dto.*;
import com.lemnt.mall.application.store.qry.StorePageQry;
import com.lemnt.mall.domain.store.gateway.StoreCatalogGateway;
import com.lemnt.mall.domain.store.model.StoreFilter;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class StoreCatalogQueryExe {
    private final StoreCatalogGateway gateway;

    private String money(java.math.BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.UNNECESSARY).toPlainString();
    }

    public List<StoreCategoryDTO> execute() {
        return gateway.categories().stream()
                .map(v -> new StoreCategoryDTO(v.id(), v.parentId(), v.name(), v.picUrl()))
                .toList();
    }

    public StorePageDTO execute(StorePageQry q) {
        if (q == null
                || q.pageNum() < 1
                || q.pageNum() > 1000000
                || q.pageSize() < 1
                || q.pageSize() > 100) throw new IllegalArgumentException("分页参数不合法");
        if (q.categoryId() != null) CatalogRules.id(q.categoryId());
        String name =
                q.name() == null || q.name().isBlank()
                        ? null
                        : CatalogRules.text(q.name(), 200, "商品名称");
        var f =
                new StoreFilter(
                        name, q.categoryId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize());
        return new StorePageDTO(
                gateway.products(f).stream()
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
                                                v.available()))
                        .toList(),
                gateway.count(f));
    }

    public StoreDetailDTO execute(Long id) {
        CatalogRules.id(id);
        var v = gateway.product(id);
        return v == null
                ? null
                : new StoreDetailDTO(
                        v.id(),
                        v.name(),
                        v.categoryId(),
                        v.brandId(),
                        v.description(),
                        v.image(),
                        v.skus().stream()
                                .map(
                                        s ->
                                                new StoreSkuDTO(
                                                        s.id(),
                                                        s.name(),
                                                        money(s.price()),
                                                        s.stock(),
                                                        s.image(),
                                                        s.properties()))
                                .toList());
    }
}

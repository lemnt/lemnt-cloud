package com.lemnt.mall.infrastructure.store.gateway;

import com.lemnt.mall.domain.store.gateway.StoreCatalogGateway;
import com.lemnt.mall.domain.store.model.*;
import com.lemnt.mall.domain.catalog.model.*;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.infrastructure.store.mapper.StoreCatalogMapper;
import java.util.List;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class StoreCatalogGatewayImpl implements StoreCatalogGateway {
    private final StoreCatalogMapper mapper;
    private final CatalogGateway catalog;

    public List<Category> categories() {
        var rows = mapper.categories();
        if (rows.size() > 1000) throw new IllegalStateException("分类超过1000条，需要分页");
        return rows.stream()
                .map(
                        v ->
                                new Category(
                                        v.getId(),
                                        v.getParentId(),
                                        v.getName(),
                                        v.getPicUrl(),
                                        v.getSort(),
                                        v.getStatus()))
                .toList();
    }

    public List<StoreProduct> products(StoreFilter f) {
        return mapper.products(f).stream()
                .map(
                        v ->
                                new StoreProduct(
                                        v.getId(),
                                        v.getName(),
                                        v.getCategoryId(),
                                        v.getBrandId(),
                                        v.getImage(),
                                        v.getMinPrice(),
                                        v.getMaxPrice(),
                                        v.isAvailable()))
                .toList();
    }

    public long count(StoreFilter f) {
        return mapper.count(f);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CatalogSpu product(Long id) {
        if (mapper.visible(id) == null) return null;
        var s = catalog.spu(id);
        return s == null
                ? null
                : new CatalogSpu(
                        s.id(),
                        s.code(),
                        s.name(),
                        s.categoryId(),
                        s.brandId(),
                        s.description(),
                        s.image(),
                        s.status(),
                        s.skus().stream()
                                .filter(
                                        v ->
                                                "ON_SHELF".equals(v.status())
                                                        && v.price() != null
                                                        && v.price().signum() > 0)
                                .toList());
    }
}

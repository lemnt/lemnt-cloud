package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.dto.StorePageDTO;
import com.lemnt.mall.application.product.dto.StoreProductDTO;
import com.lemnt.mall.application.product.qry.StorePageQry;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.StoreFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 门店目录查询执行器。
 */
@Component
@RequiredArgsConstructor
public class StoreProductPageQueryExe {
    private final ProductSpuGateway productSpuGateway;
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
        var f =
                new StoreFilter(
                name, q.categoryId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize());
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
                v.available()))
                .toList(),
                productSpuGateway.storeCount(f));
    }
}

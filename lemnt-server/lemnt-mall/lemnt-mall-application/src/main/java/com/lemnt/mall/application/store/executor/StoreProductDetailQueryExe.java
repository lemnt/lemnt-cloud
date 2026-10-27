package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.application.store.dto.StoreDetailDTO;
import com.lemnt.mall.application.store.dto.StoreSkuDTO;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 门店目录查询执行器。
 */
@Component
@RequiredArgsConstructor
public class StoreProductDetailQueryExe {
    private final CatalogService catalogService;
    private String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    /**
     * 执行门店目录查询。
     */
    public StoreDetailDTO execute(Long id) {
        CatalogRules.id(id);
        var v = catalogService.product(id);
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

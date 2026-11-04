package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.dto.StoreDetailDTO;
import com.lemnt.product.application.product.dto.StoreSkuDTO;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;

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
    private final ProductCatalogService catalogService;

    private String money(BigDecimal v) {
        return v.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    /**
     * 执行门店目录查询。
     */
    public StoreDetailDTO execute(Long id) {
        ProductCatalogValidation.id(id);
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
                            s.specs() == null
                                ? null
                                : s.specs().stream()
                                .map(
                                    property ->
                                        new StoreSkuDTO
                                            .Spec(
                                            property
                                                .specId(),
                                            property
                                                .specName(),
                                            property
                                                .specValueId(),
                                            property
                                                .specValueName()))
                                .toList()))
                .toList());
    }
}

package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.dto.ProductSpuStatusCountDTO;
import com.lemnt.mall.application.product.qry.ProductSpuCountQry;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品管理状态统计。 */
@Component
@RequiredArgsConstructor
public class ProductSpuCountQueryExe {
    private final ProductSpuGateway productSpuGateway;

    /** 名称、分类与品牌筛选和商品分页保持一致。 */
    public ProductSpuStatusCountDTO execute(ProductSpuCountQry qry) {
        if (qry == null) {
            throw new IllegalArgumentException("统计参数不能为空");
        }
        if (qry.categoryId() != null) {
            ProductCatalogValidation.id(qry.categoryId());
        }
        if (qry.brandId() != null) {
            ProductCatalogValidation.id(qry.brandId());
        }
        String name = qry.name() == null || qry.name().isBlank() ? null : ProductCatalogValidation.text(qry.name(), 200, "名称");
        var counts = productSpuGateway.counts(name, qry.categoryId(), qry.brandId());
        return new ProductSpuStatusCountDTO(counts.onShelf(), counts.offShelf(), counts.recycled(), counts.soldOut(), counts.lowStock());
    }
}

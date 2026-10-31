package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpuPageDTO;
import com.lemnt.mall.application.product.qry.ProductSpuPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductStockStatus;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 商品SPU分页查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpuPageQueryExe {
    private final ProductSpuGateway productSpuGateway;

    /**
     * 执行商品SPU分页查询。
     */
    public ProductSpuPageDTO execute(ProductSpuPageQry query) {
        if (query == null
                || query.pageNum() < 1
                || query.pageNum() > 1000000
                || query.pageSize() < 1
                || query.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        String name =
                query.name() == null || query.name().isBlank()
                ? null
                : ProductCatalogValidation.text(query.name(), 200, "商品名称");
        String status = query.status() == null || query.status().isBlank() ? null : query.status();
        if (status != null && !Set.of("ON_SHELF", "OFF_SHELF", "RECYCLED").contains(status)) {
            throw new IllegalArgumentException("商品状态不合法");
        }
        if (query.categoryId() != null) { ProductCatalogValidation.id(query.categoryId()); }
        if (query.brandId() != null) { ProductCatalogValidation.id(query.brandId()); }
        ProductStockStatus stockStatus = null;
        if (query.stockStatus() != null && !query.stockStatus().isBlank()) {
            if (!Set.of("SOLD_OUT", "LOW_STOCK").contains(query.stockStatus())) {
                throw new IllegalArgumentException("库存分组不合法");
            }
            stockStatus = ProductStockStatus.valueOf(query.stockStatus());
        }
        var page =
                productSpuGateway.page(
                new ProductCatalogFilter(
                name,
                status,
                (query.pageNum() - 1) * query.pageSize(),
                query.pageSize(), query.categoryId(), query.brandId(), stockStatus));
        return new ProductSpuPageDTO(
                page.records().stream().map(ProductCatalogDTOConverter::spu).toList(), page.total());
    }
}

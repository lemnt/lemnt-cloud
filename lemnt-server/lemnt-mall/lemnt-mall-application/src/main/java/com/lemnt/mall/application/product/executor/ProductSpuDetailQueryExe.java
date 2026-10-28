package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商品SPU详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpuDetailQueryExe {
    private final ProductCatalogService catalogService;

    /**
     * 执行商品SPU详情查询。
     */
    public ProductSpuDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductCatalogDTOConverter.spu(catalogService.spu(id));
    }
}

package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格字典详情查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecDetailQueryExe {
    private final ProductSpecGateway productSpecGateway;

    /** 查询单条字典。 */
    public ProductSpecDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductSpecDTOConverter.property(productSpecGateway.find(id));
    }
}

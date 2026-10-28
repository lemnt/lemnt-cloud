package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.converter.ProductSpecDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 规格字典详情查询。 */
@Component
@RequiredArgsConstructor
public class ProductSpecValueDetailQueryExe {
    private final ProductSpecValueGateway productSpecValueGateway;

    /** 查询单条字典。 */
    public ProductSpecValueDTO execute(Long id) {
        ProductCatalogValidation.id(id);
        return ProductSpecDTOConverter.value(productSpecValueGateway.find(id));
    }
}

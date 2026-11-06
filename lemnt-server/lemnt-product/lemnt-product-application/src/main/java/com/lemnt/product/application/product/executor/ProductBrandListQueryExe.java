package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.ProductBrandDTO;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 品牌列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductBrandListQueryExe {
    private final ProductBrandGateway brandGateway;

    /**
     * 执行品牌列表查询。
     */
    public List<ProductBrandDTO> execute() {
        return brandGateway.brands().stream().map(ProductCatalogDTOConverter::brand).toList();
    }
}

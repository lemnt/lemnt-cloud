package com.lemnt.product.application.product.executor;

import com.lemnt.product.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.product.application.product.dto.BrandDTO;
import com.lemnt.product.domain.product.gateway.BrandGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 品牌列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class BrandListQueryExe {
    private final BrandGateway brandGateway;

    /**
     * 执行品牌列表查询。
     */
    public List<BrandDTO> execute() {
        return brandGateway.brands().stream().map(ProductCatalogDTOConverter::brand).toList();
    }
}

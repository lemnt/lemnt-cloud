package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.BrandDTO;
import com.lemnt.mall.domain.catalog.gateway.BrandGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 品牌详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class BrandDetailQueryExe {
    private final BrandGateway brandGateway;

    /**
     * 执行品牌详情查询。
     */
    public BrandDTO execute(Long id) {
        CatalogRules.id(id);
        return CatalogDTOConverter.brand(brandGateway.brand(id));
    }
}

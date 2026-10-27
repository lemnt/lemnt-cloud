package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.SpuDTO;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商品SPU详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class SpuDetailQueryExe {
    private final CatalogService catalogService;

    /**
     * 执行商品SPU详情查询。
     */
    public SpuDTO execute(Long id) {
        CatalogRules.id(id);
        return CatalogDTOConverter.spu(catalogService.spu(id));
    }
}

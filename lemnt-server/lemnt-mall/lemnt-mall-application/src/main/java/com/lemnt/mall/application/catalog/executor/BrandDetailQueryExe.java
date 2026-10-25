package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrandDetailQueryExe {
    private final CatalogGateway gateway;

    public BrandDTO execute(Long id) {
        CatalogRules.id(id);
        return CatalogDTOConverter.brand(gateway.brand(id));
    }
}

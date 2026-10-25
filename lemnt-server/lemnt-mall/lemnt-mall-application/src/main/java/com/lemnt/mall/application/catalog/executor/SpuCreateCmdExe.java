package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpuCreateCmdExe {
    private final CatalogGateway gateway;
    private final com.lemnt.mall.application.catalog.service.CatalogValidationService validation;

    @Transactional(rollbackFor = Exception.class)
    public SpuDTO execute(SpuCreateCmd cmd) {
        if (cmd == null
                || cmd.skus() == null
                || cmd.skus().size() > 100
                || cmd.skus().stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("商品参数不合法");
        }
        var drafts =
                cmd.skus().stream()
                        .map(
                                s ->
                                        new CatalogSku(
                                                null,
                                                null,
                                                s.code(),
                                                s.name(),
                                                s.price(),
                                                s.stock(),
                                                s.image(),
                                                s.properties(),
                                                null))
                        .toList();
        var value =
                CatalogRules.newSpu(
                        new CatalogSpu(
                                null,
                                cmd.code(),
                                cmd.name(),
                                cmd.categoryId(),
                                cmd.brandId(),
                                cmd.description(),
                                cmd.image(),
                                null,
                                drafts));
        validation.enabledReferences(value.categoryId(), value.brandId());
        return CatalogDTOConverter.spu(gateway.createSpu(value));
    }
}

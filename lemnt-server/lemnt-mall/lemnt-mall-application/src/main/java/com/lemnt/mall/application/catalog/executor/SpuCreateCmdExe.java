package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.SpuCreateCmd;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.SpuDTO;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.application.catalog.service.CatalogValidationService;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 商品SPU创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class SpuCreateCmdExe {
    private final CatalogService catalogService;
    private final CatalogValidationService catalogValidationService;

    /**
     * 执行商品SPU创建命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public SpuDTO execute(SpuCreateCmd cmd) {
        if (cmd == null
                || cmd.skus() == null
                || cmd.skus().size() > 100
                || cmd.skus().stream().anyMatch(Objects::isNull)) {
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
        catalogValidationService.enabledReferences(value.categoryId(), value.brandId());
        return CatalogDTOConverter.spu(catalogService.createSpu(value));
    }
}

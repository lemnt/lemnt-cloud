package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd;
import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.application.product.service.ProductValidationService;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 商品SPU创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class ProductSpuCreateCmdExe {
    private final ProductCatalogService catalogService;
    private final ProductValidationService catalogValidationService;

    /**
     * 执行商品SPU创建命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpuDTO execute(ProductSpuCreateCmd cmd) {
        if (cmd == null
                || cmd.skus() == null
                || cmd.skus().size() > 100
                || cmd.skus().stream().anyMatch(Objects::isNull)
        ) {
            throw new IllegalArgumentException("商品参数不合法");
        }
        var drafts =
                cmd.skus().stream()
                .map(
                s ->
                new ProductSku(
                null,
                null,
                s.code(),
                s.name(),
                s.price(),
                s.stock(),
                s.image(),
                s.properties(),
                null, s.weight(), s.volume(), s.specValueIds()))
                .toList();
        var value =
                new ProductSpu(
                null,
                cmd.code(),
                cmd.name(),
                cmd.categoryId(),
                cmd.brandId(),
                cmd.description(),
                cmd.image(),
                null,
                drafts, cmd.deliveryTypes(), cmd.deliveryTemplateId());
        catalogValidationService.enabledReferences(value.categoryId(), value.brandId());
        return ProductCatalogDTOConverter.spu(catalogService.createSpu(value));
    }
}

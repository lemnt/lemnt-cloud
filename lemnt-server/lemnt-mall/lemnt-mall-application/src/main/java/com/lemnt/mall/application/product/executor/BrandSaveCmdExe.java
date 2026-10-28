package com.lemnt.mall.application.product.executor;

import com.lemnt.mall.application.product.cmd.BrandSaveCmd;
import com.lemnt.mall.application.product.converter.ProductCatalogDTOConverter;
import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 品牌保存命令执行器。
 */
@Component
@RequiredArgsConstructor
public class BrandSaveCmdExe {
    private final BrandGateway brandGateway;

    /**
     * 执行品牌保存命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public BrandDTO execute(BrandSaveCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("品牌参数不能为空");
        }
        if (cmd.id() != null) {
            ProductCatalogValidation.id(cmd.id());
        }
        if (cmd.sort() == null || cmd.sort() < 0) {
            throw new IllegalArgumentException("排序不能小于0");
        }
        var brand =
                new Brand(
                cmd.id(),
                ProductCatalogValidation.text(cmd.name(), 100, "品牌名称"),
                ProductCatalogValidation.optional(cmd.picUrl(), 500),
                ProductCatalogValidation.optional(cmd.description(), 2000),
                cmd.sort(),
                ProductCatalogValidation.status(cmd.status()));
        return ProductCatalogDTOConverter.brand(brandGateway.saveBrand(brand));
    }
}

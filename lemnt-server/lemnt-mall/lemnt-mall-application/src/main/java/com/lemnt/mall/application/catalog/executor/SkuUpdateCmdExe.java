package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.SkuUpdateCmd;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.product.gateway.ProductGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 商品SKU更新命令执行器。
 */
@Component
@RequiredArgsConstructor
public class SkuUpdateCmdExe {
    private final ProductGateway productGateway;

    /**
     * 执行商品SKU更新命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SkuUpdateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("SKU参数不能为空");
        }
        CatalogRules.id(cmd.id());
        return productGateway.updateSku(
                new CatalogSku(
                cmd.id(),
                null,
                null,
                CatalogRules.text(cmd.name(), 200, "SKU名称"),
                CatalogRules.price(cmd.price()),
                null,
                CatalogRules.optional(cmd.image(), 500),
                Map.of(),
                null));
    }
}

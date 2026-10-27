package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.SpuUpdateCmd;
import com.lemnt.mall.application.catalog.service.CatalogValidationService;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商品SPU更新命令执行器。
 */
@Component
@RequiredArgsConstructor
public class SpuUpdateCmdExe {
    private final SpuGateway spuGateway;
    private final CatalogValidationService catalogValidationService;

    /**
     * 执行商品SPU更新命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SpuUpdateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("商品参数不能为空");
        }
        CatalogRules.id(cmd.id());
        catalogValidationService.enabledReferences(cmd.categoryId(), cmd.brandId());
        return spuGateway.updateSpu(
                new CatalogSpu(
                cmd.id(),
                null,
                CatalogRules.text(cmd.name(), 200, "商品名称"),
                cmd.categoryId(),
                cmd.brandId(),
                CatalogRules.optional(cmd.description(), 65535),
                CatalogRules.optional(cmd.image(), 500),
                null,
                List.of()));
    }
}

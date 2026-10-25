package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpuUpdateCmdExe {
    private final CatalogGateway gateway;
    private final com.lemnt.mall.application.catalog.service.CatalogValidationService validation;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SpuUpdateCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("商品参数不能为空");
        CatalogRules.id(cmd.id());
        validation.enabledReferences(cmd.categoryId(), cmd.brandId());
        return gateway.updateSpu(
                new CatalogSpu(
                        cmd.id(),
                        null,
                        CatalogRules.text(cmd.name(), 200, "商品名称"),
                        cmd.categoryId(),
                        cmd.brandId(),
                        CatalogRules.optional(cmd.description(), 65535),
                        CatalogRules.optional(cmd.image(), 500),
                        null,
                        java.util.List.of()));
    }
}

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
public class SkuUpdateCmdExe {
    private final CatalogGateway gateway;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SkuUpdateCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("SKU参数不能为空");
        CatalogRules.id(cmd.id());
        return gateway.updateSku(
                new CatalogSku(
                        cmd.id(),
                        null,
                        null,
                        CatalogRules.text(cmd.name(), 200, "SKU名称"),
                        CatalogRules.price(cmd.price()),
                        null,
                        CatalogRules.optional(cmd.image(), 500),
                        java.util.Map.of(),
                        null));
    }
}

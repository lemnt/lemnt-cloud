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
public class SpuShelfCmdExe {
    private final CatalogGateway gateway;
    private final com.lemnt.mall.application.catalog.service.CatalogValidationService validation;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(SpuShelfCmd cmd) {
        if (cmd == null) throw new IllegalArgumentException("上架参数不能为空");
        CatalogRules.id(cmd.id());
        var value = gateway.spu(cmd.id());
        if (value == null) return false;
        if (cmd.onShelf()) {
            validation.enabledReferences(value.categoryId(), value.brandId());
            if (value.skus().isEmpty()) throw new IllegalStateException("没有SKU不能上架");
        }
        return gateway.shelf(cmd.id(), cmd.onShelf());
    }
}

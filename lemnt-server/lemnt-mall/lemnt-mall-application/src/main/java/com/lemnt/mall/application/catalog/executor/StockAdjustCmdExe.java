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
public class StockAdjustCmdExe {
    private final CatalogGateway gateway;

    @Transactional(rollbackFor = Exception.class)
    public StockAdjustmentDTO execute(StockAdjustCmd cmd) {
        if (cmd == null || cmd.delta() == 0) throw new IllegalArgumentException("库存变更量不能为0");
        CatalogRules.id(cmd.productId());
        var value =
                new StockAdjustment(
                        CatalogRules.text(cmd.requestNo(), 64, "库存请求编号"),
                        cmd.productId(),
                        cmd.delta(),
                        CatalogRules.text(cmd.reason(), 200, "库存变更原因"),
                        null);
        return CatalogDTOConverter.adjustment(gateway.adjustStock(value));
    }
}

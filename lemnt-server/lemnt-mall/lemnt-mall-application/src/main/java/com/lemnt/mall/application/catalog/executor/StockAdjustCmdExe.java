package com.lemnt.mall.application.catalog.executor;

import com.lemnt.mall.application.catalog.cmd.StockAdjustCmd;
import com.lemnt.mall.application.catalog.converter.CatalogDTOConverter;
import com.lemnt.mall.application.catalog.dto.StockAdjustmentDTO;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 库存调整命令执行器。
 */
@Component
@RequiredArgsConstructor
public class StockAdjustCmdExe {
    private final CatalogService catalogService;

    /**
     * 执行库存调整命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public StockAdjustmentDTO execute(StockAdjustCmd cmd) {
        if (cmd == null || cmd.delta() == 0) {
            throw new IllegalArgumentException("库存变更量不能为0");
        }
        CatalogRules.id(cmd.productId());
        var value =
                new StockAdjustment(
                CatalogRules.text(cmd.requestNo(), 64, "库存请求编号"),
                cmd.productId(),
                cmd.delta(),
                CatalogRules.text(cmd.reason(), 200, "库存变更原因"),
                null);
        return CatalogDTOConverter.adjustment(catalogService.adjustStock(value));
    }
}

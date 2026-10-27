package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.StockAdjustmentGateway;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.dataobject.StockAdjustmentDO;
import com.lemnt.mall.infrastructure.catalog.mapper.StockAdjustmentMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** StockAdjustment 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class StockAdjustmentGatewayImpl implements StockAdjustmentGateway {
    private final StockAdjustmentMapper stockAdjustmentMapper;

    /** 读取幂等库存调整记录。 */
    @Override
    public StockAdjustment find(String requestNo) {
        return CatalogConverter.adjustment(stockAdjustmentMapper.adjustment(requestNo));
    }

    /** 占用库存调整请求编号。 */
    @Override
    public void insert(StockAdjustment value) {
        var row = new StockAdjustmentDO();
        row.setRequestNo(value.requestNo());
        row.setProductId(value.productId());
        row.setDelta(value.delta());
        row.setReason(value.reason());
        stockAdjustmentMapper.insertAdjustment(row);
    }

    /** 记录调整后的库存。 */
    @Override
    public boolean finish(StockAdjustment value) {
        var row = new StockAdjustmentDO();
        row.setRequestNo(value.requestNo());
        row.setProductId(value.productId());
        row.setDelta(value.delta());
        row.setReason(value.reason());
        row.setResultingStock(value.resultingStock());
        return stockAdjustmentMapper.finishAdjustment(row) == 1;
    }
}

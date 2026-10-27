package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.StockReservation;
import com.lemnt.mall.infrastructure.product.dataobject.StockReservationDO;
import com.lemnt.mall.infrastructure.product.mapper.StockReservationMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** StockReservation 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class StockReservationGatewayImpl implements StockReservationGateway {
    private final StockReservationMapper stockReservationMapper;

    /** 锁定已存在的预占记录。 */
    @Override
    public StockReservation lock(String key, Long productId) {
        var row = stockReservationMapper.selectOne(Wrappers.lambdaQuery(StockReservationDO.class).eq(StockReservationDO::getReservationNo,

                key).eq(StockReservationDO::getProductId,
                productId).last("FOR UPDATE"));
        return row == null ? null : new StockReservation(row.getReservationNo(),
                row.getProductId(),
                row.getQuantity(),
                row.getStatus());
    }

    /** 保存预占或终止标记。 */
    @Override
    public void insert(StockReservation value) {
        stockReservationMapper.insert(new StockReservationDO().setReservationNo(value.reservationNo()).setProductId(value.productId()).setQuantity(value.quantity()).setStatus(value.status()));
    }

    /** 释放预占状态。 */
    @Override
    public boolean release(String key, Long productId) {
        return stockReservationMapper.releaseReservation(key, productId) == 1;
    }

    /** 确认预占状态。 */
    @Override
    public boolean confirm(String key, Long productId) {
        return stockReservationMapper.confirmReservation(key, productId) == 1;
    }
}

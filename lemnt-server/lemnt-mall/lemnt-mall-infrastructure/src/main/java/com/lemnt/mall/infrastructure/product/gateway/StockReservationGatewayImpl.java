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

    @Override
    public boolean hasActive(Long productId) {
        return stockReservationMapper.selectCount(Wrappers.lambdaQuery(StockReservationDO.class)
                .eq(StockReservationDO::getProductId, productId).eq(StockReservationDO::getStatus, "RESERVED")) > 0;
    }

    /** 锁定已存在的预占记录。 */
    @Override
    public StockReservation lock(String key, Long productId) {
        var row = stockReservationMapper.selectOne(Wrappers.lambdaQuery(StockReservationDO.class)
                .eq(StockReservationDO::getReservationNo,

                key).eq(StockReservationDO::getProductId,
                productId).last("FOR UPDATE"));
        return row == null ? null : new StockReservation(row.getReservationNo(),
                row.getProductId(),
                row.getQuantity(),
                row.getStatus(), row.getSourceNo());
    }

    /** 保存预占或终止标记。 */
    @Override
    public void insert(StockReservation value) {
        stockReservationMapper.insert(new StockReservationDO().setReservationNo(value.reservationNo())
                .setProductId(value.productId()).setQuantity(value.quantity()).setStatus(value.status()).setSourceNo(value.sourceNo()));
    }

    /** 释放预占状态。 */
    @Override
    public boolean release(String key, Long productId) {
        return stockReservationMapper.update(new StockReservationDO(), Wrappers.<StockReservationDO>lambdaUpdate()
                .set(StockReservationDO::getStatus, "RELEASED").eq(StockReservationDO::getReservationNo, key)
                .eq(StockReservationDO::getProductId, productId).eq(StockReservationDO::getStatus, "RESERVED")) == 1;
    }

    /** 确认预占状态。 */
    @Override
    public boolean confirm(String key, Long productId) {
        return stockReservationMapper.update(new StockReservationDO(), Wrappers.<StockReservationDO>lambdaUpdate()
                .set(StockReservationDO::getStatus, "CONFIRMED").eq(StockReservationDO::getReservationNo, key)
                .eq(StockReservationDO::getProductId, productId).eq(StockReservationDO::getStatus, "RESERVED")) == 1;
    }
    @Override
    public int returnedQuantity(String orderNo, Long productId) {
        return stockReservationMapper.selectList(Wrappers.<StockReservationDO>lambdaQuery()
                .eq(StockReservationDO::getSourceNo, orderNo).eq(StockReservationDO::getProductId, productId)
                .eq(StockReservationDO::getStatus, "RETURNED"))
                .stream().mapToInt(StockReservationDO::getQuantity).sum();
    }
}

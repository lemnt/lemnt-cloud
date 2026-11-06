package com.lemnt.product.infrastructure.product.gateway;

import com.lemnt.product.domain.product.gateway.ProductStockReservationGateway;
import com.lemnt.product.domain.product.model.ProductStockReservation;
import com.lemnt.product.infrastructure.product.dataobject.ProductStockReservationDO;
import com.lemnt.product.infrastructure.product.mapper.ProductStockReservationMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * ProductStockReservation 表持久化实现。
 */
@Component
@RequiredArgsConstructor
public class ProductStockReservationGatewayImpl implements ProductStockReservationGateway {
    private final ProductStockReservationMapper stockReservationMapper;

    @Override
    public boolean hasActive(Long productId) {
        return stockReservationMapper.selectCount(
            Wrappers.lambdaQuery(ProductStockReservationDO.class)
                .eq(ProductStockReservationDO::getProductId, productId)
                .eq(ProductStockReservationDO::getStatus, "RESERVED"))
            > 0;
    }

    /**
     * 锁定已存在的预占记录。
     */
    @Override
    public ProductStockReservation lock(String key, Long productId) {
        var row = stockReservationMapper.lock(key, productId);
        return row == null ? null : toModel(row);
    }

    /**
     * 无锁读取预占记录，供幂等预检与事务消息回查使用。
     */
    @Override
    public ProductStockReservation find(String key, Long productId) {
        var row =
            stockReservationMapper.selectOne(
                Wrappers.lambdaQuery(ProductStockReservationDO.class)
                    .eq(ProductStockReservationDO::getReservationNo, key)
                    .eq(ProductStockReservationDO::getProductId, productId));
        return row == null ? null : toModel(row);
    }

    private static ProductStockReservation toModel(ProductStockReservationDO row) {
        return new ProductStockReservation(
            row.getReservationNo(),
            row.getProductId(),
            row.getQuantity(),
            row.getStatus(),
            row.getSourceNo());
    }

    /**
     * 保存预占或终止标记。
     */
    @Override
    public void insert(ProductStockReservation value) {
        stockReservationMapper.insert(
            new ProductStockReservationDO()
                .setReservationNo(value.reservationNo())
                .setProductId(value.productId())
                .setQuantity(value.quantity())
                .setStatus(value.status())
                .setSourceNo(value.sourceNo()));
    }

    /**
     * 释放预占状态。
     */
    @Override
    public boolean release(String key, Long productId) {
        return stockReservationMapper.update(
            new ProductStockReservationDO(),
            Wrappers.<ProductStockReservationDO>lambdaUpdate()
                .set(ProductStockReservationDO::getStatus, "RELEASED")
                .eq(ProductStockReservationDO::getReservationNo, key)
                .eq(ProductStockReservationDO::getProductId, productId)
                .eq(ProductStockReservationDO::getStatus, "RESERVED"))
            == 1;
    }

    /**
     * 确认预占状态。
     */
    @Override
    public boolean confirm(String key, Long productId) {
        return stockReservationMapper.update(
            new ProductStockReservationDO(),
            Wrappers.<ProductStockReservationDO>lambdaUpdate()
                .set(ProductStockReservationDO::getStatus, "CONFIRMED")
                .eq(ProductStockReservationDO::getReservationNo, key)
                .eq(ProductStockReservationDO::getProductId, productId)
                .eq(ProductStockReservationDO::getStatus, "RESERVED"))
            == 1;
    }

    @Override
    public int returnedQuantity(String orderNo, Long productId) {
        return stockReservationMapper
            .selectList(
                Wrappers.<ProductStockReservationDO>lambdaQuery()
                    .eq(ProductStockReservationDO::getSourceNo, orderNo)
                    .eq(ProductStockReservationDO::getProductId, productId)
                    .eq(ProductStockReservationDO::getStatus, "RETURNED"))
            .stream()
            .mapToInt(ProductStockReservationDO::getQuantity)
            .sum();
    }
}

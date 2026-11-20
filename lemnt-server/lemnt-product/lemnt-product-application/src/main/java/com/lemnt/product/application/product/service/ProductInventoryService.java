package com.lemnt.product.application.product.service;

import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductStockReservationGateway;
import com.lemnt.product.domain.product.model.ProductStockReservation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * ProductInventoryService 跨表应用编排，保持原有事务和锁顺序。
 */
@Component
@RequiredArgsConstructor
public class ProductInventoryService {
    private final ProductSkuGateway productSkuGateway;
    private final ProductStockReservationGateway stockReservationGateway;

    private String reservationKey(String value, Long productId) {
        if (value == null
                || value.isBlank()
                || value.trim().length() > 40
                || productId == null
                || productId <= 0) {
            throw new IllegalArgumentException("库存预占编号不合法");
        }
        return value.trim();
    }

    private ProductStockReservation lockReservation(String key, Long productId) {
        if (productSkuGateway.lockById(productId) == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return stockReservationGateway.lock(key, productId);
    }

    /**
     * SKU 扣减与预占登记在商品库短事务内提交，订单后续失败时按原预占号补偿。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        String key = reservationKey(reservationNo, productId);
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("预占数量不合法");
        }
        var existing = lockReservation(key, productId);
        if (existing != null) {
            return reservedOrThrow(existing, quantity);
        }
        if (!productSkuGateway.reserveQuantity(productId, quantity)) {
            return false;
        }
        stockReservationGateway.insert(
                new ProductStockReservation(key, productId, quantity, "RESERVED"));
        return true;
    }

    private boolean reservedOrThrow(ProductStockReservation row, Integer quantity) {
        if (!quantity.equals(row.quantity())) {
            throw new IllegalStateException("预占数量不允许变化");
        }
        return "RESERVED".equals(row.status()) || "CONFIRMED".equals(row.status());
    }

    /**
     * 读取时仍按SKU及原预占行顺序加锁，避免用一级缓存误判确认结果。
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductStockReservation reservationStatus(String reservationNo, Long skuId) {
        return lockReservation(reservationKey(reservationNo, skuId), skuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(String reservationNo, Long productId, Integer quantity) {
        String key = reservationKey(reservationNo, productId);
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("预占数量不合法");
        }
        var row = lockReservation(key, productId);
        if (row == null) {
            // Terminal cancellation prevents a delayed RPC from reserving after order close.
            stockReservationGateway.insert(
                    new ProductStockReservation(
                            key,
                            productId,
                            quantity,
                            ProductStockReservation.CANCELLED_BEFORE_RESERVE));
            return;
        }
        if (!quantity.equals(row.quantity())) {
            throw new IllegalStateException("取消数量与预占不符");
        }
        releaseLocked(key, productId, row);
    }

    private void releaseLocked(String key, Long productId, ProductStockReservation row) {
        if (row.terminalRelease()) {
            return;
        }
        if (!"RESERVED".equals(row.status())) {
            throw new IllegalStateException("已确认的库存不能直接释放");
        }
        if (!stockReservationGateway.release(key, productId)
                || !productSkuGateway.releaseQuantity(productId, row.quantity())) {
            throw new IllegalStateException("库存释放失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(String reservationNo, Long productId) {
        String key = reservationKey(reservationNo, productId);
        var row = lockReservation(key, productId);
        if (row == null) {
            throw new IllegalStateException("库存预占记录不存在");
        }
        releaseLocked(key, productId, row);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmStock(String reservationNo, Long productId) {
        String key = reservationKey(reservationNo, productId);
        var row = lockReservation(key, productId);
        if (row == null) {
            throw new IllegalStateException("库存预占记录不存在");
        }
        if ("CONFIRMED".equals(row.status())) {
            return;
        }
        if (!"RESERVED".equals(row.status())) {
            throw new IllegalStateException("已取消预占不能确认");
        }
        if (!stockReservationGateway.confirm(key, productId)) {
            throw new IllegalStateException("库存确认失败");
        }
    }

    /**
     * 支付消费持有原预占锁后登记销量入账，调用方在同一事务更新销量。
     * 单独确认库存的 RPC 不会设置销量标记。
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.MANDATORY)
    public boolean confirmSale(String reservationNo, Long productId, int quantity) {
        String key = reservationKey(reservationNo, productId);
        var row = lockReservation(key, productId);
        if (row == null || row.quantity() != quantity) {
            throw new IllegalStateException("原预占数量与支付事实不一致");
        }
        if (Boolean.TRUE.equals(row.salesRecorded())) {
            return false;
        }
        if (!stockReservationGateway.recordSale(key, productId)) {
            throw new IllegalStateException("库存状态不允许记录销量");
        }
        return true;
    }

    /**
     * 售后成功按商品数量回补，原预占行串行核对累计数量，售后编号保证重试不重复。
     */
    @Transactional(rollbackFor = Exception.class)
    public void returnStock(String orderNo, String refundNo, Long productId, Integer quantity) {
        reservationKey(orderNo, productId);
        String key = reservationKey(refundNo, productId);
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("售后回补数量不合法");
        }
        var original = lockReservation(orderNo, productId);
        if (original == null || !"CONFIRMED".equals(original.status())) {
            throw new IllegalStateException("原订单库存未确认，不能售后回补");
        }
        var previous = stockReservationGateway.lock(key, productId);
        if (previous != null) {
            if (!"RETURNED".equals(previous.status())
                    || !orderNo.equals(previous.sourceNo())
                    || !quantity.equals(previous.quantity())) {
                throw new IllegalStateException("售后回补内容与原请求不一致");
            }
            return;
        }
        if ((long) stockReservationGateway.returnedQuantity(orderNo, productId) + quantity
                > original.quantity()) {
            throw new IllegalStateException("累计回补数量超过原订单数量");
        }
        if (!productSkuGateway.releaseQuantity(productId, quantity)) {
            throw new IllegalStateException("售后库存回补失败");
        }
        stockReservationGateway.insert(
                new ProductStockReservation(key, productId, quantity, "RETURNED", orderNo));
    }
}

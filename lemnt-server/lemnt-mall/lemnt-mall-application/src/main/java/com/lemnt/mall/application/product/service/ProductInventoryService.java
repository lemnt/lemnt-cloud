package com.lemnt.mall.application.product.service;

import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.StockReservation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** ProductInventoryService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class ProductInventoryService {
    private final ProductSkuGateway productSkuGateway;
    private final StockReservationGateway stockReservationGateway;
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

    private StockReservation lockReservation(String key, Long productId) {
        if (productSkuGateway.lockById(productId) == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return stockReservationGateway.lock(key, productId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        String key = reservationKey(reservationNo, productId);
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("预占数量不合法");
        }
        var existing = lockReservation(key, productId);
        if (existing != null) {
            if (!quantity.equals(existing.quantity())) {
                throw new IllegalStateException("预占数量不允许变化");
            }
            return "RESERVED".equals(existing.status())
                    || "CONFIRMED".equals(existing.status());
        }
        if (!productSkuGateway.reserveQuantity(productId, quantity)) {
            return false;
        }
        stockReservationGateway.insert(new StockReservation(key, productId, quantity, "RESERVED"));
        return true;
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
            stockReservationGateway.insert(new StockReservation(key, productId, quantity, "RELEASED"));
            return ;
        }
        if (!quantity.equals(row.quantity())) {
            throw new IllegalStateException("取消数量与预占不符");
        }
        releaseLocked(key, productId, row);
    }

    private void releaseLocked(String key, Long productId, StockReservation row) {
        if ("RELEASED".equals(row.status())) {
            return ;
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
            return ;
        }
        if (!"RESERVED".equals(row.status())) {
            throw new IllegalStateException("已取消预占不能确认");
        }
        if (!stockReservationGateway.confirm(key, productId)) {
            throw new IllegalStateException("库存确认失败");
        }
    }
    /** 售后成功按商品数量回补，原预占行串行核对累计数量，售后编号保证重试不重复。 */
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
            if (!"RETURNED".equals(previous.status()) || !orderNo.equals(previous.sourceNo())
                    || !quantity.equals(previous.quantity())) {
                throw new IllegalStateException("售后回补内容与原请求不一致");
            }
            return;
        }
        if ((long) stockReservationGateway.returnedQuantity(orderNo, productId) + quantity > original.quantity()) {
            throw new IllegalStateException("累计回补数量超过原订单数量");
        }
        if (!productSkuGateway.releaseQuantity(productId, quantity)) {
            throw new IllegalStateException("售后库存回补失败");
        }
        stockReservationGateway.insert(new StockReservation(key, productId, quantity, "RETURNED", orderNo));
    }
}

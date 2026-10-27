package com.lemnt.mall.application.product.service;

import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.StockReservation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** ProductInventoryService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class ProductInventoryService {
    private final ProductGateway productGateway;
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
        if (productGateway.lockById(productId) == null) {
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
        if (!productGateway.reserveQuantity(productId, quantity)) {
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
                || !productGateway.releaseQuantity(productId, row.quantity())) {
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
}

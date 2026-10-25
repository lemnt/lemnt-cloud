package com.lemnt.mall.infrastructure.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.domain.model.Product;
import com.lemnt.mall.infrastructure.dataobject.ProductDO;
import com.lemnt.mall.infrastructure.dataobject.StockReservationDO;
import com.lemnt.mall.infrastructure.mapper.ProductMapper;
import com.lemnt.mall.infrastructure.mapper.StockReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {
    private final ProductMapper mapper;
    private final StockReservationMapper reservations;

    @Override
    public Product findById(Long id) {
        return toDomain(mapper.selectById(id));
    }

    @Override
    public Product findSellableById(Long id) {
        return toDomain(mapper.findSellableById(id));
    }

    @Override
    public Product findByCode(String code) {
        if (code == null || code.isBlank()) return null;
        return toDomain(
                mapper.selectOne(
                        Wrappers.lambdaQuery(ProductDO.class)
                                .eq(ProductDO::getProductCode, code.trim())
                                .last("LIMIT 1")));
    }

    @Override
    public Product save(Product p) {
        ProductDO d =
                new ProductDO()
                        .setProductCode(p.productCode())
                        .setName(p.name())
                        .setDescription(p.description())
                        .setMainImage(p.mainImage())
                        .setPrice(p.price())
                        .setStock(p.stock())
                        .setStatus(p.status());
        mapper.insert(d);
        return toDomain(d);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return mapper.update(
                        null,
                        Wrappers.lambdaUpdate(ProductDO.class)
                                .eq(ProductDO::getId, id)
                                .set(ProductDO::getStatus, status))
                == 1;
    }

    @Override
    public java.util.List<Product> findSellableByIds(java.util.List<Long> ids) {
        if (ids == null || ids.size() > 200 || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("商品批量编号不合法");
        }
        if (ids.isEmpty()) return java.util.List.of();
        return mapper.findSellableByIds(ids.stream().distinct().toList()).stream()
                .map(this::toDomain)
                .toList();
    }

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

    private StockReservationDO lockReservation(String key, Long productId) {
        if (mapper.lockById(productId) == null) throw new IllegalArgumentException("商品不存在");
        return reservations.selectOne(
                Wrappers.lambdaQuery(StockReservationDO.class)
                        .eq(StockReservationDO::getReservationNo, key)
                        .eq(StockReservationDO::getProductId, productId)
                        .last("FOR UPDATE"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        String key = reservationKey(reservationNo, productId);
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("预占数量不合法");
        var existing = lockReservation(key, productId);
        if (existing != null) {
            if (!quantity.equals(existing.getQuantity()))
                throw new IllegalStateException("预占数量不允许变化");
            return "RESERVED".equals(existing.getStatus())
                    || "CONFIRMED".equals(existing.getStatus());
        }
        if (mapper.reserveStock(productId, quantity) != 1) return false;
        reservations.insert(
                new StockReservationDO()
                        .setReservationNo(key)
                        .setProductId(productId)
                        .setQuantity(quantity)
                        .setStatus("RESERVED"));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(String reservationNo, Long productId, Integer quantity) {
        String key = reservationKey(reservationNo, productId);
        if (quantity == null || quantity <= 0) throw new IllegalArgumentException("预占数量不合法");
        var row = lockReservation(key, productId);
        if (row == null) {
            // Terminal cancellation prevents a delayed RPC from reserving after order close.
            reservations.insert(
                    new StockReservationDO()
                            .setReservationNo(key)
                            .setProductId(productId)
                            .setQuantity(quantity)
                            .setStatus("RELEASED"));
            return;
        }
        if (!quantity.equals(row.getQuantity())) throw new IllegalStateException("取消数量与预占不符");
        releaseLocked(key, productId, row);
    }

    private void releaseLocked(String key, Long productId, StockReservationDO row) {
        if ("RELEASED".equals(row.getStatus())) return;
        if (!"RESERVED".equals(row.getStatus())) throw new IllegalStateException("已确认的库存不能直接释放");
        if (reservations.releaseReservation(key, productId) != 1
                || mapper.releaseStock(productId, row.getQuantity()) != 1) {
            throw new IllegalStateException("库存释放失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(String reservationNo, Long productId) {
        String key = reservationKey(reservationNo, productId);
        var row = lockReservation(key, productId);
        if (row == null) throw new IllegalStateException("库存预占记录不存在");
        releaseLocked(key, productId, row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStock(String reservationNo, Long productId) {
        String key = reservationKey(reservationNo, productId);
        var row = lockReservation(key, productId);
        if (row == null) throw new IllegalStateException("库存预占记录不存在");
        if ("CONFIRMED".equals(row.getStatus())) return;
        if (!"RESERVED".equals(row.getStatus())) throw new IllegalStateException("已取消预占不能确认");
        if (reservations.confirmReservation(key, productId) != 1)
            throw new IllegalStateException("库存确认失败");
    }

    private Product toDomain(ProductDO d) {
        return d == null
                ? null
                : new Product(
                        d.getId(),
                        d.getProductCode(),
                        d.getName(),
                        d.getDescription(),
                        d.getMainImage(),
                        d.getPrice(),
                        d.getStock(),
                        d.getStatus(),
                        d.getCreateTime());
    }
}

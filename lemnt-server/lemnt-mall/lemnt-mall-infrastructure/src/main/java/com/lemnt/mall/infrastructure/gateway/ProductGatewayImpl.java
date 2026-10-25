package com.lemnt.mall.infrastructure.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.domain.model.Product;
import com.lemnt.mall.infrastructure.dataobject.ProductDO;
import com.lemnt.mall.infrastructure.dataobject.StockReservationDO;
import com.lemnt.mall.infrastructure.mapper.ProductMapper;
import com.lemnt.mall.infrastructure.mapper.StockReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
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
    @Transactional(rollbackFor = Exception.class)
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        if (reservationNo == null
                || reservationNo.isBlank()
                || productId == null
                || quantity == null
                || quantity <= 0) {
            throw new IllegalArgumentException("库存预占参数不合法");
        }
        StockReservationDO existing = findReservation(reservationNo, productId);
        if (existing != null) {
            if (!quantity.equals(existing.getQuantity())) {
                throw new IllegalStateException("同一预占单号不能使用不同的商品数量");
            }
            return "RESERVED".equals(existing.getStatus())
                    || "CONFIRMED".equals(existing.getStatus());
        }

        // Conditional SQL prevents stock from going negative under concurrent requests.
        if (mapper.reserveStock(productId, quantity) != 1) return false;
        StockReservationDO reservation =
                new StockReservationDO()
                        .setReservationNo(reservationNo.trim())
                        .setProductId(productId)
                        .setQuantity(quantity)
                        .setStatus("RESERVED");
        try {
            reservations.insert(reservation);
        } catch (DuplicateKeyException duplicate) {
            // The transaction must roll back the stock decrement. The caller may retry;
            // the next request will observe the durable reservation and be idempotent.
            throw duplicate;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(String reservationNo, Long productId) {
        StockReservationDO reservation = findReservation(reservationNo, productId);
        if (reservation == null) throw new IllegalStateException("库存预占记录不存在，无法释放");
        if ("RELEASED".equals(reservation.getStatus())) return;
        if ("CONFIRMED".equals(reservation.getStatus())) {
            throw new IllegalStateException("已确认的库存预占不能直接释放");
        }
        if (reservations.releaseReservation(reservationNo, productId) == 1) {
            if (mapper.releaseStock(productId, reservation.getQuantity()) != 1) {
                throw new IllegalStateException("库存回补失败");
            }
            return;
        }
        StockReservationDO latest = findReservation(reservationNo, productId);
        if (latest != null && "RELEASED".equals(latest.getStatus())) return;
        throw new IllegalStateException("库存释放状态冲突");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStock(String reservationNo, Long productId) {
        StockReservationDO reservation = findReservation(reservationNo, productId);
        if (reservation == null) throw new IllegalStateException("库存预占记录不存在，无法确认");
        if ("CONFIRMED".equals(reservation.getStatus())) return;
        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new IllegalStateException("已释放的库存预占不能确认");
        }
        if (reservations.confirmReservation(reservationNo, productId) != 1) {
            StockReservationDO latest = findReservation(reservationNo, productId);
            if (latest != null && "CONFIRMED".equals(latest.getStatus())) return;
            throw new IllegalStateException("库存确认状态冲突");
        }
    }

    private StockReservationDO findReservation(String reservationNo, Long productId) {
        if (reservationNo == null || reservationNo.isBlank() || productId == null) return null;
        return reservations.selectOne(
                Wrappers.lambdaQuery(StockReservationDO.class)
                        .eq(StockReservationDO::getReservationNo, reservationNo.trim())
                        .eq(StockReservationDO::getProductId, productId)
                        .last("LIMIT 1"));
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

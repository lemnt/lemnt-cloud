package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogRules;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.dataobject.StockAdjustmentDO;
import com.lemnt.mall.infrastructure.product.dataobject.ProductDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Product 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class ProductGatewayImpl implements ProductGateway {
    private final ProductMapper productMapper;

    @Override
    public boolean updateSku(CatalogSku value) {
        return productMapper.updateSku(CatalogConverter.sku(value)) == 1;
    }

    @Override
    public Product findById(Long id) {
        return toDomain(productMapper.selectById(id));
    }

    @Override
    public Product findSellableById(Long id) {
        return toDomain(productMapper.findSellableById(id));
    }

    @Override
    public Product findByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return toDomain(
                productMapper.selectOne(
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
        productMapper.insert(d);
        return toDomain(d);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return productMapper.update(
                null,
                Wrappers.lambdaUpdate(ProductDO.class)
                .eq(ProductDO::getId, id)
                .set(ProductDO::getStatus, status))
        == 1;
    }

    @Override
    public List<Product> findSellableByIds(List<Long> ids) {
        if (ids == null || ids.size() > 200 || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("商品批量编号不合法");
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        return productMapper.findSellableByIds(ids.stream().distinct().toList()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    /**
     * 事实。
     */
    public List<CatalogPricingFacts> facts(List<Long> ids) {
        return productMapper.facts(PromotionCatalogRules.ids(ids, 100)).stream()
                .map(
                r -> {
                long fen = r.getPrice().movePointRight(2).longValueExact();
                if (fen <= 0
                    || r.getStock() == null
                    || r.getStock() < 0
                    || r.getSpuId() == null
                    || r.getSpuId() <= 0
                    || r.getProductCode() == null
                    || r.getName() == null) {
                throw new IllegalStateException("Invalid catalog facts");
                }
                return new CatalogPricingFacts(
                    r.getSkuId(),
                    r.getSpuId(),
                    r.getProductCode(),
                    r.getName(),
                    fen,
                    r.getStock());
                })
                .toList();
    }

    @Override
    /**
     * 已有商品SKU编号集合。
     */
    public List<Long> existingSkuIds(List<Long> ids) {
        return productMapper.existingSkuIds(PromotionCatalogRules.ids(ids, 1000));
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

    /** 锁定商品行，保持库存事务锁顺序。 */
    @Override
    public Product lockById(Long id) {
        return toDomain(productMapper.lockById(id));
    }

    /** 扣减可用库存。 */
    @Override
    public boolean reserveQuantity(Long id, Integer quantity) {
        return productMapper.reserveStock(id, quantity) == 1;
    }

    /** 归还预占库存。 */
    @Override
    public boolean releaseQuantity(Long id, Integer quantity) {
        return productMapper.releaseStock(id, quantity) == 1;
    }

    /** 读取指定商品的 SKU 列表。 */
    @Override
    public List<CatalogSku> catalogSkus(Long spuId) {
        return productMapper.skus(spuId).stream().map(row -> CatalogConverter.sku(row, List.of())).toList();
    }

    /** 读取单个 SKU。 */
    @Override
    public CatalogSku catalogSku(Long id) {
        return CatalogConverter.sku(productMapper.sku(id), List.of());
    }

    /** 保存 SKU 主体，属性由属性表组件维护。 */
    @Override
    public CatalogSku insertSku(CatalogSku value) {
        var row = CatalogConverter.sku(value);
        productMapper.insertSku(row);
        return CatalogConverter.sku(row, List.of());
    }

    /** 按原有边界校验调整库存。 */
    @Override
    public boolean adjustQuantity(StockAdjustment value) {
        var row = new StockAdjustmentDO();
        row.setProductId(value.productId());
        row.setDelta(value.delta());
        return productMapper.adjustStock(row) == 1;
    }
}

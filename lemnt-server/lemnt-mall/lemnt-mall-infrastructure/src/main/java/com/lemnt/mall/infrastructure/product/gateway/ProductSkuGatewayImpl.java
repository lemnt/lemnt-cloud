package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSkuFilter;
import com.lemnt.mall.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSkuMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/** Product 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class ProductSkuGatewayImpl implements ProductSkuGateway {
    private final ProductSkuMapper productSkuMapper;

    @Override
    public List<ProductSku> byIds(List<Long> ids) {
        if (ids.isEmpty()
        ) {
            return List.of();
        }
        return productSkuMapper.selectByIds(ids).stream()
                .map(row -> ProductCatalogConverter.sku(row, List.of())).toList();
    }

    @Override
    public List<ProductSku> skusBySpuIds(List<Long> ids) {
        if (ids.isEmpty()
        ) {
            return List.of();
        }
        return productSkuMapper.selectList(Wrappers.<ProductSkuDO>lambdaQuery()
                .in(ProductSkuDO::getSpuId, ids).ne(ProductSkuDO::getStatus, "DELETED").orderByAsc(ProductSkuDO::getId))
                .stream().map(row -> ProductCatalogConverter.sku(row, List.of())).toList();
    }

    @Override
    public PageResult<ProductSku> skuPage(ProductSkuFilter filter) {
        var page = productSkuMapper.selectPage(new Page<>(
                filter.pageNum(), filter.pageSize()), Wrappers.lambdaQuery(ProductSkuDO.class)
                .isNotNull(ProductSkuDO::getSpuId).ne(ProductSkuDO::getStatus, "DELETED")
                .eq(filter.spuId() != null, ProductSkuDO::getSpuId, filter.spuId())
                .eq(filter.code() != null, ProductSkuDO::getProductCode, filter.code())
                .like(filter.name() != null, ProductSkuDO::getName, filter.name())
                .eq(filter.status() != null, ProductSkuDO::getStatus, filter.status())
                .orderByDesc(ProductSkuDO::getId));
        return PageResult.of(page.getRecords().stream().map(row -> new ProductSku(
                row.getId(), row.getSpuId(), row.getProductCode(), row.getName(), row.getPrice(), row.getStock(),
                row.getMainImage(), Map.of(), row.getStatus(), row.getWeight(), row.getVolume())).toList(), page.getTotal());
    }

    @Override
    public boolean deleteSku(Long id) {
        return productSkuMapper.update(null, Wrappers.lambdaUpdate(ProductSkuDO.class).eq(ProductSkuDO::getId, id)
                .ne(ProductSkuDO::getStatus, "DELETED").set(ProductSkuDO::getStatus, "DELETED")
                .set(ProductSkuDO::getVariantKey, null)) == 1;
    }

    @Override
    public void clearVariantKeys(Long spuId) {
        productSkuMapper.update(null, Wrappers.<ProductSkuDO>lambdaUpdate()
                .eq(ProductSkuDO::getSpuId, spuId).ne(ProductSkuDO::getStatus, "DELETED")
                .set(ProductSkuDO::getVariantKey, null));
    }

    @Override
    public boolean updateSku(ProductSku value) {
        var row = ProductCatalogConverter.sku(value);
        return productSkuMapper.update(new ProductSkuDO(), Wrappers.<ProductSkuDO>lambdaUpdate()
                .set(ProductSkuDO::getName, row.getName()).set(ProductSkuDO::getPrice, row.getPrice())
                .set(ProductSkuDO::getMainImage, row.getMainImage())
                .set(ProductSkuDO::getStock, row.getStock())
                .set(ProductSkuDO::getWeight, row.getWeight())
                .set(ProductSkuDO::getVolume, row.getVolume())
                .set(ProductSkuDO::getStatus, "ON_SHELF")
                .set(ProductSkuDO::getVariantKey, row.getVariantKey())
                .eq(ProductSkuDO::getId, row.getId())
                .ne(ProductSkuDO::getStatus, "DELETED")) == 1;
    }

    @Override
    public Product findById(Long id) {
        return toDomain(productSkuMapper.selectById(id));
    }

    @Override
    public Product findSellableById(Long id) {
        return toDomain(productSkuMapper.findSellableById(id));
    }

    @Override
    public Product findByCode(String code) {
        if (code == null || code.isBlank()
        ) {
            return null;
        }
        return toDomain(
                productSkuMapper.selectOne(
                Wrappers.lambdaQuery(ProductSkuDO.class)
                .eq(ProductSkuDO::getProductCode, code.trim())
                .last("LIMIT 1")));
    }

    @Override
    public Product save(Product p) {
        ProductSkuDO d =
                new ProductSkuDO()
                .setProductCode(p.productCode())
                .setName(p.name())
                .setDescription(p.description())
                .setMainImage(p.mainImage())
                .setPrice(p.price())
                .setStock(p.stock())
                .setStatus(p.status());
        productSkuMapper.insert(d);
        return toDomain(d);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return productSkuMapper.update(
                null,
                Wrappers.lambdaUpdate(ProductSkuDO.class)
                .eq(ProductSkuDO::getId, id)
                .ne(ProductSkuDO::getStatus, "DELETED")
                .set(ProductSkuDO::getStatus, status))
        == 1;
    }

    @Override
    public List<Product> findSellableByIds(List<Long> ids) {
        if (ids == null || ids.size() > 200 || ids.stream().anyMatch(id -> id == null || id <= 0)
        ) {
            throw new IllegalArgumentException("商品批量编号不合法");
        }
        if (ids.isEmpty()
        ) {
            return List.of();
        }
        return productSkuMapper.findSellableByIds(ids.stream().distinct().toList()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    /**
     * 已有商品SKU编号集合。
     */
    public List<Long> existingSkuIds(List<Long> ids) {
        return productSkuMapper.selectList(Wrappers.<ProductSkuDO>lambdaQuery().select(ProductSkuDO::getId)
                .in(ProductSkuDO::getId, ProductCatalogValidation.ids(ids, 1000)).ne(ProductSkuDO::getStatus, "DELETED")
                .orderByAsc(ProductSkuDO::getId))
                .stream().map(ProductSkuDO::getId).toList();
    }

    private Product toDomain(ProductSkuDO d) {
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
        return toDomain(productSkuMapper.lockById(id));
    }

    /** 扣减可用库存。 */
    @Override
    public boolean reserveQuantity(Long id, Integer quantity) {
        return productSkuMapper.reserveStock(id, quantity) == 1;
    }

    /** 归还预占库存。 */
    @Override
    public boolean releaseQuantity(Long id, Integer quantity) {
        return productSkuMapper.update(new ProductSkuDO(), Wrappers.<ProductSkuDO>lambdaUpdate()
                .setIncrBy(ProductSkuDO::getStock, quantity).eq(ProductSkuDO::getId, id)) == 1;
    }

    /** 读取指定商品的 SKU 列表。 */
    @Override
    public List<ProductSku> catalogSkus(Long spuId) {
        return productSkuMapper.selectList(Wrappers.<ProductSkuDO>lambdaQuery()
                .eq(ProductSkuDO::getSpuId, spuId).ne(ProductSkuDO::getStatus, "DELETED")
                .orderByAsc(ProductSkuDO::getId))
                .stream().map(row -> ProductCatalogConverter.sku(row, List.of())).toList();
    }

    /** 读取单个 SKU。 */
    @Override
    public ProductSku catalogSku(Long id) {
        return ProductCatalogConverter.sku(productSkuMapper.selectById(id), List.of());
    }

    /** 保存 SKU 主体，属性由属性表组件维护。 */
    @Override
    public ProductSku insertSku(ProductSku value) {
        var row = ProductCatalogConverter.sku(value);
        productSkuMapper.insert(row);
        return ProductCatalogConverter.sku(row, List.of());
    }

    /** 按原有边界校验调整库存。 */
}

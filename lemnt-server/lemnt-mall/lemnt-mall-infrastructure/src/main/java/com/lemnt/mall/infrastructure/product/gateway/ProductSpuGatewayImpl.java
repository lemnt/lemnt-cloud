package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogPage;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.product.model.ProductSpuStatusCount;
import com.lemnt.mall.domain.product.model.ProductStockStatus;
import com.lemnt.mall.domain.product.model.StoreFilter;
import com.lemnt.mall.domain.product.model.StoreProduct;
import com.lemnt.mall.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpuDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSpuMapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Spu 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class ProductSpuGatewayImpl implements ProductSpuGateway {
    private final ProductSpuMapper productSpuMapper;

    @Override
    public List<ProductSpu> findSpus(List<Long> ids) {
        if (ids.isEmpty()
        ) {
            return List.of();
        }
        return productSpuMapper.selectList(Wrappers
                .lambdaQuery(ProductSpuDO.class)
                .in(ProductSpuDO::getId, ids)
                .ne(ProductSpuDO::getStatus, "DELETED"))
                .stream().map(row -> ProductCatalogConverter.spu(row, List.of())).toList();
    }

    @Override
    public ProductSpu lockSpu(Long id) {
        return ProductCatalogConverter.spu(productSpuMapper.lockSpu(id), List.of());
    }

    @Override
    public boolean transition(Long id, String expected, String target) {
        return productSpuMapper.update(null, Wrappers
                .lambdaUpdate(ProductSpuDO.class)
                .eq(ProductSpuDO::getId, id)
                .eq(ProductSpuDO::getStatus, expected)
                .set(ProductSpuDO::getStatus, target)) == 1;
    }

    @Override
    public ProductSpuStatusCount counts(String name, Long categoryId, Long brandId) {
        var onShelf = new ProductCatalogFilter(name, "ON_SHELF", 0, 1, categoryId, brandId, null);
        var offShelf = new ProductCatalogFilter(name, "OFF_SHELF", 0, 1, categoryId, brandId, null);
        var recycled = new ProductCatalogFilter(name, "RECYCLED", 0, 1, categoryId, brandId, null);
        return new ProductSpuStatusCount(productSpuMapper.selectCount(query(onShelf)),
                productSpuMapper.selectCount(query(offShelf)), productSpuMapper.selectCount(query(recycled)),
                productSpuMapper.countStockSpus(new ProductCatalogFilter(name, null, 0, 1, categoryId, brandId,
                        ProductStockStatus.SOLD_OUT), ProductStockStatus.ALERT_STOCK),
                productSpuMapper.countStockSpus(new ProductCatalogFilter(name, null, 0, 1, categoryId, brandId,
                        ProductStockStatus.LOW_STOCK), ProductStockStatus.ALERT_STOCK));
    }

    @Override
    public List<ProductSpu> simpleList(String name, int limit) {
        var filter = new ProductCatalogFilter(name, "ON_SHELF", 0, limit, null, null, null);
        return productSpuMapper.selectPage(new Page<>(1, limit, false), query(filter).orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId))
                .getRecords().stream().map(row -> ProductCatalogConverter.spu(row, List.of())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCatalogPage page(ProductCatalogFilter filter) {
        var pagination = new Page<ProductSpuDO>((long) filter.offset() / filter.limit() + 1, filter.limit());
        var page = filter.stockStatus() == null
                ? productSpuMapper.selectPage(pagination, query(filter).orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId))
                : productSpuMapper.selectStockPage(pagination, filter, ProductStockStatus.ALERT_STOCK);
        return new ProductCatalogPage(page.getRecords().stream()
                .map(row -> ProductCatalogConverter.spu(row, List.of())).toList(), page.getTotal());
    }

    @Override
    public boolean updateSpu(ProductSpu value) {
        var row = ProductCatalogConverter.spu(value);
        return productSpuMapper.update(null, Wrappers.<ProductSpuDO>lambdaUpdate()
                .set(ProductSpuDO::getName, row.getName())
                .set(row.getKeyword() != null, ProductSpuDO::getKeyword, row.getKeyword())
                .set(row.getIntroduction() != null, ProductSpuDO::getIntroduction, row.getIntroduction())
                .set(row.getSliderPicUrls() != null, ProductSpuDO::getSliderPicUrls, row.getSliderPicUrls())
                .set(row.getSort() != null, ProductSpuDO::getSort, row.getSort())
                .set(ProductSpuDO::getCategoryId, row.getCategoryId())
                .set(ProductSpuDO::getBrandId, row.getBrandId())
                .set(ProductSpuDO::getDescription, row.getDescription())
                .set(ProductSpuDO::getImage, row.getImage())
                .set(ProductSpuDO::getDeliveryTypes, row.getDeliveryTypes())
                .set(ProductSpuDO::getDeliveryTemplateId, row.getDeliveryTemplateId())
                .eq(ProductSpuDO::getId, row.getId())
                .in(ProductSpuDO::getStatus, "ON_SHELF", "OFF_SHELF")) == 1;
    }

    @Override
    public boolean shelf(Long id, boolean onShelf) {
        return productSpuMapper.update(null, Wrappers.<ProductSpuDO>lambdaUpdate()
                .set(ProductSpuDO::getStatus, (onShelf ? "ON_SHELF" : "OFF_SHELF"))
                .eq(ProductSpuDO::getId, id)
                .in(ProductSpuDO::getStatus, "ON_SHELF", "OFF_SHELF")) == 1;
    }

    @Override
    /**
     * 查询商品。
     */
    public List<StoreProduct> products(StoreFilter f) {
        return productSpuMapper.products(f).stream()
                .map(
                v ->
                new StoreProduct(
                v.getId(),
                v.getName(),
                v.getCategoryId(),
                v.getBrandId(),
                v.getImage(),
                v.getMinPrice(),
                v.getMaxPrice(),
                v.isAvailable()))
                .toList();
    }

    @Override
    /**
     * 数量。
     */
    public long storeCount(StoreFilter f) {
        return productSpuMapper.storeCatalogCount(f);
    }

    @Override
    /**
     * 已有商品SPU编号集合。
     */
    public List<Long> existingSpuIds(List<Long> ids) {
        return productSpuMapper.selectList(Wrappers.<ProductSpuDO>lambdaQuery().select(ProductSpuDO::getId)
                .in(ProductSpuDO::getId, ProductCatalogValidation.ids(ids, 1000)).ne(ProductSpuDO::getStatus, "DELETED")
                .orderByAsc(ProductSpuDO::getId))
                .stream().map(ProductSpuDO::getId).toList();
    }

    /** 保存商品主体，不写入 SKU 或属性。 */
    @Override
    public ProductSpu insertSpu(ProductSpu value) {
        var row = ProductCatalogConverter.spu(value);
        productSpuMapper.insert(row);
        return ProductCatalogConverter.spu(row, List.of());
    }

    /** 读取商品主体。 */
    @Override
    public ProductSpu findSpu(Long id) {
        return ProductCatalogConverter.spu(productSpuMapper.selectById(id), List.of());
    }

    /** 检查商品在小程序是否可见。 */
    @Override
    public boolean visible(Long id) {
        return productSpuMapper.visible(id) != null;
    }

    /** 统一商品列表、状态统计和选择查询的过滤条件。 */
    private LambdaQueryWrapper<ProductSpuDO> query(ProductCatalogFilter filter) {
        return Wrappers.<ProductSpuDO>lambdaQuery()
                .like(filter.name() != null, ProductSpuDO::getName, filter.name())
                .eq(filter.status() != null, ProductSpuDO::getStatus, filter.status())
                .ne(ProductSpuDO::getStatus, "DELETED")
                .eq(filter.categoryId() != null, ProductSpuDO::getCategoryId, filter.categoryId())
                .eq(filter.brandId() != null, ProductSpuDO::getBrandId, filter.brandId());
    }
}

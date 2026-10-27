package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.CatalogFilter;
import com.lemnt.mall.domain.catalog.model.CatalogPage;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.promotion.model.PromotionCatalogRules;
import com.lemnt.mall.domain.store.model.StoreFilter;
import com.lemnt.mall.domain.store.model.StoreProduct;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.mapper.SpuMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Spu 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class SpuGatewayImpl implements SpuGateway {
    private final SpuMapper spuMapper;

    @Override
    @Transactional(readOnly = true)
    public CatalogPage page(CatalogFilter filter) {
        return new CatalogPage(
                spuMapper.page(filter).stream()
                .map(row -> CatalogConverter.spu(row, List.of()))
                .toList(),
                spuMapper.catalogCount(filter));
    }

    @Override
    public boolean updateSpu(CatalogSpu value) {
        return spuMapper.updateSpu(CatalogConverter.spu(value)) == 1;
    }

    @Override
    public boolean shelf(Long id, boolean onShelf) {
        return spuMapper.shelf(id, onShelf ? "ON_SHELF" : "OFF_SHELF") == 1;
    }

    @Override
    /**
     * 查询商品。
     */
    public List<StoreProduct> products(StoreFilter f) {
        return spuMapper.products(f).stream()
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
        return spuMapper.storeCatalogCount(f);
    }

    @Override
    /**
     * 已有商品SPU编号集合。
     */
    public List<Long> existingSpuIds(List<Long> ids) {
        return spuMapper.existingSpuIds(PromotionCatalogRules.ids(ids, 1000));
    }

    /** 保存商品主体，不写入 SKU 或属性。 */
    @Override
    public CatalogSpu insertSpu(CatalogSpu value) {
        var row = CatalogConverter.spu(value);
        spuMapper.insertSpu(row);
        return CatalogConverter.spu(row, List.of());
    }

    /** 读取商品主体。 */
    @Override
    public CatalogSpu findSpu(Long id) {
        return CatalogConverter.spu(spuMapper.spu(id), List.of());
    }

    /** 检查商品在小程序是否可见。 */
    @Override
    public boolean visible(Long id) {
        return spuMapper.visible(id) != null;
    }
}

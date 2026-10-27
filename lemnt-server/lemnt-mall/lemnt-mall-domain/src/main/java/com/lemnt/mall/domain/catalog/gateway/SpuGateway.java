package com.lemnt.mall.domain.catalog.gateway;

import com.lemnt.mall.domain.catalog.model.CatalogFilter;
import com.lemnt.mall.domain.catalog.model.CatalogPage;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.store.model.StoreFilter;
import com.lemnt.mall.domain.store.model.StoreProduct;

import java.util.List;

/** Spu 表领域存储接口。 */
public interface SpuGateway {
    /**
     * 转换分页查询或分页响应。
     */
    CatalogPage page(CatalogFilter filter);

    /**
     * 更新商品SPU。
     */
    boolean updateSpu(CatalogSpu value);

    /**
     * 上架状态。
     */
    boolean shelf(Long id, boolean onShelf);

    /**
     * 查询商品。
     */
    List<StoreProduct> products(StoreFilter filter);

    /**
     * 数量。
     */
    long storeCount(StoreFilter filter);

    /**
     * 已有商品SPU编号集合。
     */
    List<Long> existingSpuIds(List<Long> ids);

    /** 保存商品主体，不写入 SKU 或属性。 */
    CatalogSpu insertSpu(CatalogSpu value);

    /** 读取商品主体。 */
    CatalogSpu findSpu(Long id);

    /** 检查商品在小程序是否可见。 */
    boolean visible(Long id);
}

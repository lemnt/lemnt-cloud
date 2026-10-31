package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.ProductCatalogFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogPage;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.product.model.ProductSpuStatusCount;
import com.lemnt.mall.domain.product.model.StoreFilter;
import com.lemnt.mall.domain.product.model.StoreProduct;

import java.util.List;

/** Spu 表领域存储接口。 */
public interface ProductSpuGateway {
    /** 批量查询未删除的SPU主体。 */
    List<ProductSpu> findSpus(List<Long> ids);

    /** 锁定SPU，串行管理状态与SKU维护。 */
    ProductSpu lockSpu(Long id);

    /** 条件切换状态，删除使用终态保留历史ID。 */
    boolean transition(Long id, String expected, String target);

    /** 按筛选条件统计非删除商品状态。 */
    ProductSpuStatusCount counts(String name, Long categoryId, Long brandId);

    /** 查询有限条在售商品供选择。 */
    List<ProductSpu> simpleList(String name, int limit);

    /**
     * 转换分页查询或分页响应。
     */
    ProductCatalogPage page(ProductCatalogFilter filter);

    /**
     * 更新商品SPU。
     */
    boolean updateSpu(ProductSpu value);

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
    ProductSpu insertSpu(ProductSpu value);

    /** 读取商品主体。 */
    ProductSpu findSpu(Long id);

    /** 检查商品在小程序是否可见。 */
    boolean visible(Long id);
}

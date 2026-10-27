package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.promotion.model.CatalogPricingFacts;

import java.util.List;

/** Product 表领域存储接口。 */
public interface ProductGateway {
    /**
     * 更新商品SKU。
     */
    boolean updateSku(CatalogSku value);

    /**
     * 查找按编号。
     */
    Product findById(Long id);

    /**
     * 查找可售商品按编号。
     */
    Product findSellableById(Long id);

    /**
     * 查找可售商品按编号集合。
     */
    List<Product> findSellableByIds(List<Long> ids);

    /**
     * 查找按编码。
     */
    Product findByCode(String code);

    /**
     * 保存。
     */
    Product save(Product product);

    /**
     * 更新状态。
     */
    boolean updateStatus(Long id, String status);

    /**
     * 事实。
     */
    List<CatalogPricingFacts> facts(List<Long> ids);

    /**
     * 已有商品SKU编号集合。
     */
    List<Long> existingSkuIds(List<Long> ids);

    /** 锁定商品行，保持库存事务锁顺序。 */
    Product lockById(Long id);

    /** 扣减可用库存。 */
    boolean reserveQuantity(Long id, Integer quantity);

    /** 归还预占库存。 */
    boolean releaseQuantity(Long id, Integer quantity);

    /** 读取指定商品的 SKU 列表。 */
    List<CatalogSku> catalogSkus(Long spuId);

    /** 读取单个 SKU。 */
    CatalogSku catalogSku(Long id);

    /** 保存 SKU 主体，属性由属性表组件维护。 */
    CatalogSku insertSku(CatalogSku value);

    /** 按原有边界校验调整库存。 */
    boolean adjustQuantity(StockAdjustment value);
}

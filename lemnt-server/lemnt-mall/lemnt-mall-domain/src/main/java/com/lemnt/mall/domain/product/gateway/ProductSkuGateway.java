package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSkuFilter;

import java.util.List;

/** Product 表领域存储接口。 */
public interface ProductSkuGateway {
    /** 批量读取SKU配送字段。 */
    List<ProductSku> byIds(List<Long> ids);

    /** 批量查询SPU的未删除SKU。 */
    List<ProductSku> skusBySpuIds(List<Long> ids);

    /** 管理SKU分页，不包含永久删除状态。 */
    PageResult<ProductSku> skuPage(ProductSkuFilter filter);

    /** 标记删除并解除规格唯一键，原SKU编码和历史记录保留。 */
    boolean deleteSku(Long id);

    /** 整体保存前释放当前商品规格唯一键，允许同一事务内交换规格。 */
    void clearVariantKeys(Long spuId);

    /**
     * 更新商品SKU。
     */
    boolean updateSku(ProductSku value);

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
    List<ProductSku> catalogSkus(Long spuId);

    /** 读取单个 SKU。 */
    ProductSku catalogSku(Long id);

    /** 保存SKU主体及规格JSON。 */
    ProductSku insertSku(ProductSku value);

    /** 同步规格项或规格值名称，只修改当前SKU。 */
    void renameProperty(Long propertyId, String name);

    /** 同步规格值名称，保留SKU编号和已有订单快照。 */
    void renamePropertyValue(Long valueId, String name);

}

package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.ProductFavorite;

/** mall_product_favorite领域存储。 */
public interface ProductFavoriteGateway {
    /** 读取本人唯一收藏。 */
    ProductFavorite find(Long userId, Long spuId);
    /** 保存收藏，调用方持有基础会员锁至事务结束。 */
    ProductFavorite save(Long userId, Long spuId);
    /** 只删除指定会员的收藏。 */
    boolean remove(Long userId, Long spuId);
    /** 收藏数量。 */
    long count(Long userId);
    /** 按真实会员/商品筛选分页。 */
    PageResult<ProductFavorite> page(Long userId, Long spuId, int pageNum, int pageSize);
}

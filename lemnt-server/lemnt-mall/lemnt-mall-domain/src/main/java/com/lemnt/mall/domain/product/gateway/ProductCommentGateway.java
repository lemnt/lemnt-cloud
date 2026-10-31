package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.PublicCommentFilter;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.CommentFilter;
import com.lemnt.mall.domain.product.model.ProductComment;

/** 商品评价表领域存储接口。 */
public interface ProductCommentGateway {
    /** 查询详情。 */
    ProductComment find(Long id);

    /** 锁定回复记录。 */
    ProductComment lock(Long id);

    /** 按真实订单项读取唯一评价。 */
    ProductComment findByOrderItem(Long orderItemId);

    /** 条件绑定交易确认的旧行，不能改写已有不同归属。 */
    boolean bindOrderItem(com.lemnt.mall.domain.product.model.CommentOrderLink value);

    /** 只查询尚未绑定ID的旧订单SKU评价，绑定前由交易核验SKU唯一性。 */
    ProductComment findByOrderSku(String orderNo, Long skuId);

    /** 保存真实订单评价。 */
    ProductComment save(ProductComment value);

    /** 商家回复。 */
    boolean reply(ProductComment value);

    /** 更新是否展示。 */
    boolean visible(Long id, boolean visible);

    /** 公开分页固定可见，仅按商品及评分组查询。 */
    PageResult<ProductComment> pageVisible(PublicCommentFilter filter);

    /** 后台分页。 */
    PageResult<ProductComment> page(CommentFilter filter);
}

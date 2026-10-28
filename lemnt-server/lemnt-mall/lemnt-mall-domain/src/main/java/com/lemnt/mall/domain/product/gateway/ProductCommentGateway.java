package com.lemnt.mall.domain.product.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.product.model.CommentFilter;
import com.lemnt.mall.domain.product.model.ProductComment;

/** 商品评价表领域存储接口。 */
public interface ProductCommentGateway {
    /** 查询详情。 */
    ProductComment find(Long id);

    /** 锁定回复记录。 */
    ProductComment lock(Long id);

    /** 查询同一订单SKU已有评价，用于原请求重放。 */
    ProductComment findByOrderSku(String orderNo, Long skuId);

    /** 保存真实订单评价。 */
    ProductComment save(ProductComment value);

    /** 商家回复。 */
    boolean reply(ProductComment value);

    /** 更新是否展示。 */
    boolean visible(Long id, boolean visible);

    /** 后台分页。 */
    PageResult<ProductComment> page(CommentFilter filter);
}

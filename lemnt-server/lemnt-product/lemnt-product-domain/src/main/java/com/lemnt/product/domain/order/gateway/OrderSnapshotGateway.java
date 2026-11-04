package com.lemnt.product.domain.order.gateway;

import com.lemnt.product.domain.product.model.CommentOrderRequest;

/**
 * 评价入口委托订单服务持锁验证购买事实，商品服务不访问订单表。
 */
public interface OrderSnapshotGateway {
    /**
     * 使用原订单项幂等投稿；订单服务再调用已验证的商品评价入口。
     */
    Long createComment(CommentOrderRequest request);
}

package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CartReference;

import java.util.List;

/** Cart 表领域存储接口。 */
public interface CartGateway {
    /**
     * 购物车。
     */
    List<CartLine> cart(Long memberId);

    /** 保存购物车条目，由应用层先获取会员结算锁。 */
    CartLine saveCart(Long memberId, Long productId, int quantity, boolean selected);

    /** 删除购物车条目，由应用层先获取会员结算锁。 */
    boolean deleteCart(Long memberId, Long productId);

    /** 消费已提交的购物车版本。 */
    void consume(Long memberId, CartReference reference);
}

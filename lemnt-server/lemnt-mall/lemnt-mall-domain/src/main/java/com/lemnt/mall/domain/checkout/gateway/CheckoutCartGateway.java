package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.CartReference;

import java.util.List;

/** CheckoutCart 表领域存储接口。 */
public interface CheckoutCartGateway {
    /** 读取结算占用的购物车版本。 */
    List<CartReference> references(String orderNo);

    /** 保存结算购物车版本引用。 */
    void saveReferences(String orderNo, List<CartReference> references);

    /** 检查结算对购物车版本的占用。 */
    boolean cartVersionConsumed(CartReference reference);

    /** 检查结算对购物车版本的占用。 */
    boolean cartInCheckout(Long cartId);
}

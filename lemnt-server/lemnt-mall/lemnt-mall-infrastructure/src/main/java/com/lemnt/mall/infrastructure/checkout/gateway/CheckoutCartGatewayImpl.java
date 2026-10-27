package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CheckoutCartGateway;
import com.lemnt.mall.domain.checkout.model.CartReference;
import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutCartDO;
import com.lemnt.mall.infrastructure.checkout.mapper.CheckoutCartMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** CheckoutCart 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CheckoutCartGatewayImpl implements CheckoutCartGateway {
    private final CheckoutCartMapper checkoutCartMapper;
    private final com.lemnt.mall.domain.order.gateway.OrderGateway orders;

    /** 读取结算占用的购物车版本。 */
    @Override
    public List<CartReference> references(String orderNo) {
        return checkoutCartMapper.references(orderNo).stream().map(row -> new CartReference(row.getCartId(),
                row.getCartVersion())).toList();
    }

    /** 保存结算购物车版本引用。 */
    @Override
    public void saveReferences(String orderNo, List<CartReference> references) {
        for (var value : references) {
            var row = new CheckoutCartDO();
            row.setOrderNo(orderNo);
            row.setCartId(value.cartId());
            row.setCartVersion(value.cartVersion());
            checkoutCartMapper.insertReference(row);
        }
    }

    @Override
    public boolean cartVersionConsumed(CartReference reference) {
        return checkoutCartMapper.ordersForCart(reference.cartId()).stream()
                .filter(row -> row.getCartVersion() == reference.cartVersion())
                .anyMatch(row -> {
                    var order = orders.findByOrderNo(row.getOrderNo());
                    return order == null || !"CLOSED".equals(order.status());
                });
    }

    @Override
    public boolean cartInCheckout(Long cartId) {
        return checkoutCartMapper.ordersForCart(cartId).stream().anyMatch(row -> {
            var order = orders.findByOrderNo(row.getOrderNo());
            return order == null || "RESERVING".equals(order.status());
        });
    }
}

package com.lemnt.order.domain.cart.gateway;
import com.lemnt.order.domain.cart.model.CartLine;
import com.lemnt.order.domain.cart.model.CartReference;

import java.util.List;
/** 订单只读选中购物车及按原版本消费，不拥有购物车表。 */
public interface CartSnapshotGateway {
    List<CartLine> cart(Long memberId);
    void consume(String orderNo,Long memberId,List<CartReference> references);
}

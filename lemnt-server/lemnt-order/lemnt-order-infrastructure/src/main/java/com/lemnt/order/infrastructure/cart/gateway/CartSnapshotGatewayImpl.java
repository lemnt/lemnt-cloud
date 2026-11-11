package com.lemnt.order.infrastructure.cart.gateway;

import com.lemnt.order.domain.cart.gateway.CartSnapshotGateway;
import com.lemnt.order.domain.cart.model.CartLine;
import com.lemnt.order.domain.cart.model.CartReference;
import com.lemnt.promotion.client.CartClient;
import com.lemnt.promotion.client.bo.CartConsumeBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 精确传递原购物车版本，营销服务自行提交幂等消费。
 */
@Component
public class CartSnapshotGatewayImpl implements CartSnapshotGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private CartClient client;

    public List<CartLine> cart(Long memberId) {
        return client.cart(memberId).stream()
            .map(
                v ->
                    new CartLine(
                        v.id(),
                        v.memberId(),
                        v.productId(),
                        v.quantity(),
                        v.selected(),
                        v.version()))
            .toList();
    }

    public void consume(String no, Long member, List<CartReference> refs) {
        client.consume(
            new CartConsumeBo(
                no,
                member,
                refs.stream()
                    .map(v -> new CartConsumeBo.Reference(v.cartId(), v.cartVersion()))
                    .toList()));
    }
}

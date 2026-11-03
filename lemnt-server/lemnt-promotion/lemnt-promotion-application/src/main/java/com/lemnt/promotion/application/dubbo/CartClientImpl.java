package com.lemnt.promotion.application.dubbo;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.promotion.client.CartClient;
import com.lemnt.promotion.client.bo.CartConsumeBo;
import com.lemnt.promotion.client.dto.CartSnapshotDTO;
import com.lemnt.promotion.domain.cart.gateway.CartGateway;
import com.lemnt.promotion.domain.cart.model.CartReference;
import com.lemnt.promotion.domain.event.gateway.MessageInboxGateway;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 购物车版本消费与本库幂等凭证同事务，失败不留下成功标记。 */
@DubboService
@RequiredArgsConstructor
public class CartClientImpl implements CartClient {
    private final CartGateway carts;
    private final MessageInboxGateway inbox;

    @Override
    public List<CartSnapshotDTO> cart(Long memberId) {
        if (memberId == null || memberId <= 0) throw new IllegalArgumentException("会员编号不合法");
        return carts.cart(memberId).stream()
                .map(
                        v ->
                                new CartSnapshotDTO(
                                        v.id(),
                                        v.memberId(),
                                        v.productId(),
                                        v.quantity(),
                                        v.selected(),
                                        v.version()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consume(CartConsumeBo input) {
        if (input == null
                || input.orderNo() == null
                || input.orderNo().isBlank()
                || input.memberId() == null
                || input.references() == null) throw new IllegalArgumentException("购物车消费引用不合法");
        if (!inbox.begin(
                "promotion.cart.consume.v1",
                input.orderNo(),
                "CART_CONSUME",
                input.orderNo(),
                JsonUtils.toJsonStr(input))) return;
        for (var ref : input.references())
            carts.consume(input.memberId(), new CartReference(ref.cartId(), ref.version()));
    }
}

package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartSaveCmdExe {
    private final CheckoutGateway checkout;
    private final com.lemnt.trade.application.checkout.service.CheckoutMemberService members;
    private final com.lemnt.trade.domain.gateway.ProductCatalogGateway products;

    @Transactional(rollbackFor = Exception.class)
    public CartItemDTO execute(CartSaveCmd cmd) {

        if (cmd == null || cmd.quantity() < 1 || cmd.quantity() > 999)
            throw new IllegalArgumentException("购物车数量必须为1至999");
        CheckoutRules.id(cmd.productId());
        members.requireActive(cmd.memberId());
        checkout.lockMember(cmd.memberId());
        var snapshot = products.findOnShelf(cmd.productId());
        if (snapshot == null
                && (cmd.selected()
                        || checkout.cart(cmd.memberId()).stream()
                                .noneMatch(row -> row.productId().equals(cmd.productId())))) {
            throw new IllegalArgumentException("商品不存在或已下架，只能取消已有条目的选中状态");
        }
        return CheckoutDTOConverter.cart(
                checkout.saveCart(cmd.memberId(), cmd.productId(), cmd.quantity(), cmd.selected()),
                snapshot);
    }
}

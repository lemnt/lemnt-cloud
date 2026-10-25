package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartListQueryExe {
    private final CheckoutGateway checkout;
    private final com.lemnt.trade.domain.gateway.ProductCatalogGateway products;

    public java.util.List<CartItemDTO> execute(Long memberId) {
        CheckoutRules.id(memberId);
        var rows = checkout.cart(memberId);
        var snapshots = products.findOnShelfBatch(rows.stream().map(CartLine::productId).toList());
        return rows.stream()
                .map(row -> CheckoutDTOConverter.cart(row, snapshots.get(row.productId())))
                .toList();
    }
}

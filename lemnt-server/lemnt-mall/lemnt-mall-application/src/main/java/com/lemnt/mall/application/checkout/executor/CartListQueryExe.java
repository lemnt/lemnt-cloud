package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CartItemDTO;
import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 购物车列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CartListQueryExe {
    private final CartGateway cartGateway;
    private final ProductCatalogGateway productCatalogGateway;

    /**
     * 执行购物车列表查询。
     */
    public List<CartItemDTO> execute(Long memberId) {
        CheckoutRules.id(memberId);
        var rows = cartGateway.cart(memberId);
        var snapshots = productCatalogGateway.findOnShelfBatch(rows.stream().map(CartLine::productId).toList());
        return rows.stream()
                .map(row -> CheckoutDTOConverter.cart(row, snapshots.get(row.productId())))
                .toList();
    }
}

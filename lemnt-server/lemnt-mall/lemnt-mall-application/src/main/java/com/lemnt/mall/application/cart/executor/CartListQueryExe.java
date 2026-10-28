package com.lemnt.mall.application.cart.executor;

import com.lemnt.mall.application.cart.converter.CartDTOConverter;
import com.lemnt.mall.application.cart.dto.CartItemDTO;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.cart.model.CartLine;
import com.lemnt.mall.domain.cart.model.CartRules;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;

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
        CartRules.id(memberId);
        var rows = cartGateway.cart(memberId);
        var snapshots = productCatalogGateway.findOnShelfBatch(rows.stream().map(CartLine::productId).toList());
        return rows.stream()
                .map(row -> CartDTOConverter.cart(row, snapshots.get(row.productId())))
                .toList();
    }
}

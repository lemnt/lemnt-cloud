package com.lemnt.trade.infrastructure.checkout.dataobject;

import lombok.Data;

@Data
public class CartDO {
    private Long id;
    private Long memberId;
    private Long productId;
    private Integer quantity;
    private Boolean selected;
    private Long version;
}

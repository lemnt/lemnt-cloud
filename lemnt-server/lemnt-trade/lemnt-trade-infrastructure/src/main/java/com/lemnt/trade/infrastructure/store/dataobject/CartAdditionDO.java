package com.lemnt.trade.infrastructure.store.dataobject;

@lombok.Data
public class CartAdditionDO {
    private Long memberId;
    private String requestNo;
    private Long productId;
    private int quantity;
    private Long cartId;
    private int resultQuantity;
    private boolean selected;
    private long cartVersion;
}

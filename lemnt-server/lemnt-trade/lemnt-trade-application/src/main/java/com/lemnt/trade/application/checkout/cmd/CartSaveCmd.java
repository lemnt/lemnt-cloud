package com.lemnt.trade.application.checkout.cmd;

public record CartSaveCmd(Long memberId, Long productId, int quantity, boolean selected) {}

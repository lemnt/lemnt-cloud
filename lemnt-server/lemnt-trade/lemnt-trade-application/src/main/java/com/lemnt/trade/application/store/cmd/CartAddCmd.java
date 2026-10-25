package com.lemnt.trade.application.store.cmd;

public record CartAddCmd(Long memberId, Long productId, int quantity, String requestNo) {}

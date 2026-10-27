package com.lemnt.mall.application.checkout.cmd;

import java.util.List;

/**
 * 结算提交命令。
 */
public record CheckoutSubmitCmd(
        Long memberId,
        String requestNo,
        List<Long> productIds,
        Long couponId,
        String quoteToken
) implements java.io.Serializable {}

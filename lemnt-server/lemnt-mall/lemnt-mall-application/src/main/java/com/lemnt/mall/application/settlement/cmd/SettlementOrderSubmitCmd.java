package com.lemnt.mall.application.settlement.cmd;

import java.util.List;

/**
 * 结算提交命令。
 */
public record SettlementOrderSubmitCmd(
        Long memberId,
        String requestNo,
        List<Long> productIds,
        Long couponId,
        String quoteToken
) implements java.io.Serializable {}

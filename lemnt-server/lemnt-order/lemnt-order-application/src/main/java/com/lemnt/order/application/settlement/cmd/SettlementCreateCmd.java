package com.lemnt.order.application.settlement.cmd;

import java.io.Serializable;
import java.util.List;

/** SettlementCreateCmd 输入契约。 */
public record SettlementCreateCmd(
        Long memberId,
        List<Long> productIds,
        Long couponId,
        Boolean pointStatus,
        Long addressId,
        Integer deliveryType,
        Long pickUpStoreId)
        implements Serializable {}

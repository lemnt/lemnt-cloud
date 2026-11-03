package com.lemnt.order.application.brokerage.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 分销用户输出。 */
public record BrokerageUserDTO(
        Long id,
        Long bindUserId,
        String bindNickname,
        String bindAvatar,
        Boolean brokerageEnabled,
        BigDecimal brokeragePrice,
        BigDecimal frozenPrice,
        long firstLevelCount,
        long secondLevelCount)
        implements Serializable {}

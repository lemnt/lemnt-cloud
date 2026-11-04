package com.lemnt.order.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 原审批提现金额，支付只核对并按原提现号入账。 */
public record BrokerageWithdrawalDTO(
        Long id, Long memberId, BigDecimal amount, Integer type, Integer status)
        implements Serializable {}

package com.lemnt.pay.client.bo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 原提现号是稳定幂等键，不接受调用方覆盖原审核金额。
 */
public record BrokerageWithdrawalBo(Long id, Long memberId, BigDecimal amount)
    implements Serializable {
}

package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 钱包账户分页筛选。 */
public record WalletFilter(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {
}

package com.lemnt.pay.domain.payment.model;

import java.io.Serializable;

/**
 * 钱包流水分页筛选。
 */
public record WalletTransactionFilter(Long walletId, String bizType, int pageNum, int pageSize)
    implements Serializable {
}

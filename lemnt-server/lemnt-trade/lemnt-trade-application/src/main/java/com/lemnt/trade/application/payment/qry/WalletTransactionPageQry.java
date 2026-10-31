package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 会员钱包流水分页查询。 */
public record WalletTransactionPageQry(
        String bizType,
        int pageNum,
        int pageSize
) implements Serializable {
}

package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 钱包账户分页查询。 */
public record WalletPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {
}

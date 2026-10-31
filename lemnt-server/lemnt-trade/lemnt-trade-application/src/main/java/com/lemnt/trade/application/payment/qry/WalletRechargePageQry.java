package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 充值单分页查询。 */
public record WalletRechargePageQry(
        Long memberId,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

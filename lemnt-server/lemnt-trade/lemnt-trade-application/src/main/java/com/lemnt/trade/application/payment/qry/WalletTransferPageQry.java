package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 钱包转账分页查询。 */
public record WalletTransferPageQry(
        Long memberId,
        Long fromMemberId,
        Long toMemberId,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 钱包转账分页筛选；memberId 用于查询本人转出或转入。 */
public record WalletTransferFilter(
        Long memberId,
        Long fromMemberId,
        Long toMemberId,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

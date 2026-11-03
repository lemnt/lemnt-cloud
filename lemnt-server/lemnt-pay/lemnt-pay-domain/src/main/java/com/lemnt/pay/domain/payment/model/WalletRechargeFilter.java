package com.lemnt.pay.domain.payment.model;

import java.io.Serializable;

/** 充值单分页筛选。 */
public record WalletRechargeFilter(Long memberId, String status, int pageNum, int pageSize)
        implements Serializable {}

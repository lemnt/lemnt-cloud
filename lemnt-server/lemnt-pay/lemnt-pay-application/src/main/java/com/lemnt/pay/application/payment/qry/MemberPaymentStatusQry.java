package com.lemnt.pay.application.payment.qry;

import java.io.Serializable;

/**
 * 会员支付状态查询。
 */
public record MemberPaymentStatusQry(Long memberId, String orderNo) implements Serializable {
}

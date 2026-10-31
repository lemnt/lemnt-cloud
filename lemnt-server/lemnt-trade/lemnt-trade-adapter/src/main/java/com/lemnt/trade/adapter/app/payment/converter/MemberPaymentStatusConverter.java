package com.lemnt.trade.adapter.app.payment.converter;

import com.lemnt.trade.adapter.app.payment.res.MemberPaymentStatusRes;
import com.lemnt.trade.application.payment.dto.MemberPaymentStatusDTO;
import com.lemnt.trade.application.payment.qry.MemberPaymentStatusQry;

/**
 * 会员支付状态转换器。
 */
public final class MemberPaymentStatusConverter {
    private MemberPaymentStatusConverter() {}

    /**
     * 查询。
     */
    public static MemberPaymentStatusQry query(Long memberId, String no) {
        return new MemberPaymentStatusQry(memberId, no);
    }

    /**
     * 响应。
     */
    public static MemberPaymentStatusRes response(MemberPaymentStatusDTO v) {
        return new MemberPaymentStatusRes(
                v.orderNo(),
                v.orderStatus(),
                v.payableAmount(),
                v.localPaymentStatus(),
                v.channelVerified());
    }
}

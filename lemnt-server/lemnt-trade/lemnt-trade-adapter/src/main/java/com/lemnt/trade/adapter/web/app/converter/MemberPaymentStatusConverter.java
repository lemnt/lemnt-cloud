package com.lemnt.trade.adapter.web.app.converter;

import com.lemnt.trade.adapter.web.app.res.MemberPaymentStatusRes;
import com.lemnt.trade.application.checkout.dto.MemberPaymentStatusDTO;
import com.lemnt.trade.application.checkout.qry.MemberPaymentStatusQry;

public final class MemberPaymentStatusConverter {
    private MemberPaymentStatusConverter() {}

    public static MemberPaymentStatusQry query(Long memberId, String no) {
        return new MemberPaymentStatusQry(memberId, no);
    }

    public static MemberPaymentStatusRes response(MemberPaymentStatusDTO v) {
        return new MemberPaymentStatusRes(
                v.orderNo(),
                v.orderStatus(),
                v.payableAmount(),
                v.localPaymentStatus(),
                v.channelVerified());
    }
}

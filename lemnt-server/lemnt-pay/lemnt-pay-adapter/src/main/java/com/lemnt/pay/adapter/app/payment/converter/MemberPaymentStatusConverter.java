package com.lemnt.pay.adapter.app.payment.converter;

import com.lemnt.pay.adapter.app.payment.req.AppPaymentCreateReq;
import com.lemnt.pay.adapter.app.payment.res.AppPaymentPrepayRes;
import com.lemnt.pay.adapter.app.payment.res.MemberPaymentStatusRes;
import com.lemnt.pay.application.payment.cmd.PaymentPrepayCmd;
import com.lemnt.pay.application.payment.dto.MemberPaymentStatusDTO;
import com.lemnt.pay.application.payment.dto.PaymentPrepayDTO;
import com.lemnt.pay.application.payment.qry.MemberPaymentStatusQry;

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

    /** 会员编号取自会话，客户端只选择应用与渠道。 */
    public static PaymentPrepayCmd prepay(Long memberId, String orderNo, AppPaymentCreateReq req) {
        return new PaymentPrepayCmd(memberId, orderNo, req.appKey(), req.channel());
    }

    /** 预支付响应。 */
    public static AppPaymentPrepayRes prepay(PaymentPrepayDTO value) {
        return new AppPaymentPrepayRes(
                value.paymentNo(),
                value.orderNo(),
                value.channel(),
                value.amount(),
                value.payUrl(),
                value.params());
    }
}

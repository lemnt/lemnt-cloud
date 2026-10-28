package com.lemnt.trade.adapter.web.payment.converter;

import com.lemnt.trade.adapter.web.payment.req.PaymentPageReq;
import com.lemnt.trade.application.payment.qry.PaymentPageQry;

/** 后台查询请求转换。 */
public final class PaymentReqConverter {
    private PaymentReqConverter() {
    }

    /** 转换分页查询并补齐默认页码。 */
    public static PaymentPageQry page(PaymentPageReq req) {
        return new PaymentPageQry(req.paymentNo(), req.orderNo(), req.channel(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}

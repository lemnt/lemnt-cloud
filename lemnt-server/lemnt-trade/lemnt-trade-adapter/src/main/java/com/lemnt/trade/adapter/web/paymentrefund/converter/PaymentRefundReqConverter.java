package com.lemnt.trade.adapter.web.paymentrefund.converter;

import com.lemnt.trade.adapter.web.paymentrefund.req.PaymentRefundPageReq;
import com.lemnt.trade.application.paymentrefund.qry.PaymentRefundPageQry;

/** 后台查询请求转换。 */
public final class PaymentRefundReqConverter {
    private PaymentRefundReqConverter() {
    }

    /** 转换分页查询并补齐默认页码。 */
    public static PaymentRefundPageQry page(PaymentRefundPageReq req) {
        return new PaymentRefundPageQry(req.afterSaleNo(), req.paymentNo(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}

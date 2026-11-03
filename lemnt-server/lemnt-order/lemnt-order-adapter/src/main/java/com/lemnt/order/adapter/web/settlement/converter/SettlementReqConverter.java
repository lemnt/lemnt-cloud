package com.lemnt.order.adapter.web.settlement.converter;

import com.lemnt.order.adapter.web.settlement.req.SettlementOrderSubmitReq;
import com.lemnt.order.adapter.web.settlement.req.SettlementQuoteReq;
import com.lemnt.order.application.settlement.cmd.SettlementOrderSubmitCmd;
import com.lemnt.order.application.settlement.qry.SettlementQuoteQry;

/** 按业务域转换接口与应用数据。 */
public final class SettlementReqConverter {
    private SettlementReqConverter() {}

    /**
     * 报价。
     */
    public static SettlementQuoteQry quote(Long memberId, SettlementQuoteReq req) {
        return new SettlementQuoteQry(memberId, req.productIds(), req.couponId());
    }

    /**
     * 结算。
     */
    public static SettlementOrderSubmitCmd checkout(Long memberId, SettlementOrderSubmitReq req) {
        return new SettlementOrderSubmitCmd(
                memberId, req.requestNo(), req.productIds(), req.couponId(), req.quoteToken());
    }
}

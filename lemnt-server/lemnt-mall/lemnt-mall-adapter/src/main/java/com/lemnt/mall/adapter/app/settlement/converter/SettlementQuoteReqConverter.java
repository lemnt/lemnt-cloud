package com.lemnt.mall.adapter.app.settlement.converter;

import com.lemnt.mall.adapter.app.settlement.req.SettlementQuoteReq;
import com.lemnt.mall.application.settlement.cmd.SettlementCreateCmd;

/** 按业务域转换接口与应用数据。 */
public final class SettlementQuoteReqConverter {
    private SettlementQuoteReqConverter() {}

    /**
     * 报价。
     */
    public static SettlementCreateCmd quote(Long memberId,
            SettlementQuoteReq req
    ) {
        return new SettlementCreateCmd(memberId,
                req.productIds(),
                req.couponId(), req.pointStatus(), req.addressId());
    }
}

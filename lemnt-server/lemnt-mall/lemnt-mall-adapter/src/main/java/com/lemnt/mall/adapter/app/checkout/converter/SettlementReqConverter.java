package com.lemnt.mall.adapter.app.checkout.converter;

import com.lemnt.mall.adapter.app.checkout.req.MallAppCheckoutAbandonReq;
import com.lemnt.mall.adapter.app.checkout.req.MallAppCheckoutCheckoutReq;
import com.lemnt.mall.application.checkout.cmd.SettlementAbandonCmd;
import com.lemnt.mall.application.checkout.cmd.SettlementSubmitCmd;
import com.lemnt.mall.application.checkout.qry.SettlementRequestDetailQry;

/** 结算接口输入转换。 */
public final class SettlementReqConverter {
    private SettlementReqConverter() {}

    /** 转换提单命令。 */
    public static SettlementSubmitCmd submit(Long memberId, MallAppCheckoutCheckoutReq req) {
        return new SettlementSubmitCmd(memberId, req.settlementId(), req.requestNo());
    }

    /** 转换终止提单命令。 */
    public static SettlementAbandonCmd abandon(Long memberId, MallAppCheckoutAbandonReq req) {
        return new SettlementAbandonCmd(memberId, req.settlementId(), req.requestNo());
    }

    /** 转换本人请求查询。 */
    public static SettlementRequestDetailQry request(Long memberId, String requestNo) {
        return new SettlementRequestDetailQry(memberId, requestNo);
    }
}

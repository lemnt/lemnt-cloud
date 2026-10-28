package com.lemnt.mall.adapter.app.settlement.converter;

import com.lemnt.mall.adapter.app.settlement.req.SettlementAbandonReq;
import com.lemnt.mall.adapter.app.settlement.req.SettlementSubmitReq;
import com.lemnt.mall.application.settlement.cmd.SettlementAbandonCmd;
import com.lemnt.mall.application.settlement.cmd.SettlementSubmitCmd;
import com.lemnt.mall.application.settlement.qry.SettlementRequestDetailQry;

/** 结算接口输入转换。 */
public final class SettlementReqConverter {
    private SettlementReqConverter() {}

    /** 转换提单命令。 */
    public static SettlementSubmitCmd submit(Long memberId, SettlementSubmitReq req) {
        return new SettlementSubmitCmd(memberId, req.settlementId(), req.requestNo());
    }

    /** 转换终止提单命令。 */
    public static SettlementAbandonCmd abandon(Long memberId, SettlementAbandonReq req) {
        return new SettlementAbandonCmd(memberId, req.settlementId(), req.requestNo());
    }

    /** 转换本人请求查询。 */
    public static SettlementRequestDetailQry request(Long memberId, String requestNo) {
        return new SettlementRequestDetailQry(memberId, requestNo);
    }
}

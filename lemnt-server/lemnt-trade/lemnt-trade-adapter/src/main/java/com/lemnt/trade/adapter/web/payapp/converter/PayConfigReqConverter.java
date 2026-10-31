package com.lemnt.trade.adapter.web.payapp.converter;

import com.lemnt.trade.adapter.web.payapp.req.PayAppCreateReq;
import com.lemnt.trade.adapter.web.payapp.req.PayAppPageReq;
import com.lemnt.trade.adapter.web.payapp.req.PayAppUpdateReq;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelCreateReq;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelPageReq;
import com.lemnt.trade.adapter.web.paychannel.req.PayChannelUpdateReq;
import com.lemnt.trade.application.payment.cmd.PayAppCreateCmd;
import com.lemnt.trade.application.payment.cmd.PayAppUpdateCmd;
import com.lemnt.trade.application.payment.cmd.PayChannelCreateCmd;
import com.lemnt.trade.application.payment.cmd.PayChannelUpdateCmd;
import com.lemnt.trade.application.payment.qry.PayAppPageQry;
import com.lemnt.trade.application.payment.qry.PayChannelPageQry;

/** 支付应用与渠道配置请求转换。 */
public final class PayConfigReqConverter {
    private PayConfigReqConverter() {
    }

    public static PayAppCreateCmd payAppCreate(PayAppCreateReq req) {
        return new PayAppCreateCmd(req.appKey(), req.name(), req.status(), req.remark(),
                req.orderNotifyUrl(), req.refundNotifyUrl());
    }

    public static PayAppUpdateCmd payAppUpdate(Long id, PayAppUpdateReq req) {
        return new PayAppUpdateCmd(id, req.name(), req.status(), req.remark(), req.orderNotifyUrl(),
                req.refundNotifyUrl());
    }

    public static PayAppPageQry payAppPage(PayAppPageReq req) {
        return new PayAppPageQry(req.appKey(), req.name(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static PayChannelCreateCmd payChannelCreate(PayChannelCreateReq req) {
        return new PayChannelCreateCmd(req.appId(), req.code(), req.status(), req.feeRate(), req.remark(),
                req.config());
    }

    public static PayChannelUpdateCmd payChannelUpdate(Long id, PayChannelUpdateReq req) {
        return new PayChannelUpdateCmd(id, req.status(), req.feeRate(), req.remark(), req.config());
    }

    public static PayChannelPageQry payChannelPage(PayChannelPageReq req) {
        return new PayChannelPageQry(req.appId(), req.code(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}

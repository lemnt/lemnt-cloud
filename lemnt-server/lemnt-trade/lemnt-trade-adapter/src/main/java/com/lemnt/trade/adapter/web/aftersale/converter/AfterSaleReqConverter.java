package com.lemnt.trade.adapter.web.aftersale.converter;

import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleCreateReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSalePageReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleRefuseReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleRejectReq;
import com.lemnt.trade.adapter.web.aftersale.req.AfterSaleReturnReq;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRefuseCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRejectCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.trade.application.aftersale.qry.AfterSalePageQry;

/** 售后请求与应用输入转换。 */
public final class AfterSaleReqConverter {
    private AfterSaleReqConverter() {
    }

    /** 转换商品售后申请。 */
    public static AfterSaleCreateCmd create(String orderNo, AfterSaleCreateReq req) {
        return new AfterSaleCreateCmd(orderNo, req.requestNo(), req.orderItemId(), req.quantity(), req.type(), req.reason());
    }

    /** 转换拒绝请求。 */
    public static AfterSaleRejectCmd reject(String no, AfterSaleRejectReq req) {
        return new AfterSaleRejectCmd(no, req.reason());
    }

    /** 转换拒收请求。 */
    public static AfterSaleRefuseCmd refuse(String no, AfterSaleRefuseReq req) {
        return new AfterSaleRefuseCmd(no, req.reason());
    }

    /** 转换退货运单请求。 */
    public static AfterSaleReturnCmd returned(String no, AfterSaleReturnReq req) {
        return new AfterSaleReturnCmd(no, req.carrierCode(), req.trackingNo());
    }

    /** 转换分页请求。 */
    public static AfterSalePageQry page(AfterSalePageReq req) {
        return new AfterSalePageQry(req.orderNo(), req.status(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}

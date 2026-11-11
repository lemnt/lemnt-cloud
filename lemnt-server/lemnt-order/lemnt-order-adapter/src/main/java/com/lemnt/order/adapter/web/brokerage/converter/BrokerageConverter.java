package com.lemnt.order.adapter.web.brokerage.converter;

import com.lemnt.order.adapter.web.brokerage.req.BrokerageBindReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageEnabledReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageRecordPageReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageUserPageReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageWithdrawAuditReq;
import com.lemnt.order.adapter.web.brokerage.req.BrokerageWithdrawPageReq;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageRecordRes;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageUserRes;
import com.lemnt.order.adapter.web.brokerage.res.BrokerageWithdrawRes;
import com.lemnt.order.application.brokerage.cmd.BrokerageAdminBindCmd;
import com.lemnt.order.application.brokerage.cmd.BrokerageEnabledCmd;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.dto.BrokerageRecordDTO;
import com.lemnt.order.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.order.application.brokerage.dto.BrokerageWithdrawDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageRecordPageQry;
import com.lemnt.order.application.brokerage.qry.BrokerageUserPageQry;
import com.lemnt.order.application.brokerage.qry.BrokerageWithdrawPageQry;

/**
 * 管理端分销请求及响应转换。
 */
public final class BrokerageConverter {
    private BrokerageConverter() {
    }

    public static BrokerageUserPageQry users(BrokerageUserPageReq req) {
        return new BrokerageUserPageQry(
            req.bindUserId(),
            req.brokerageEnabled(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BrokerageRecordPageQry records(BrokerageRecordPageReq req) {
        return new BrokerageRecordPageQry(
            req.userId(),
            req.bizType(),
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BrokerageWithdrawPageQry withdraws(BrokerageWithdrawPageReq req) {
        return new BrokerageWithdrawPageQry(
            req.userId(),
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BrokerageEnabledCmd enabled(Long id, BrokerageEnabledReq req) {
        return new BrokerageEnabledCmd(id, req.enabled());
    }

    public static BrokerageAdminBindCmd bind(Long id, BrokerageBindReq req) {
        return new BrokerageAdminBindCmd(id, req.bindUserId());
    }

    public static BrokerageWithdrawAuditCmd audit(
        Long id, Long operatorId, BrokerageWithdrawAuditReq req) {
        return new BrokerageWithdrawAuditCmd(id, req.approved(), req.reason(), operatorId);
    }

    public static BrokerageUserRes user(BrokerageUserDTO value) {
        return new BrokerageUserRes(
            value.id(),
            value.bindUserId(),
            value.bindNickname(),
            value.bindAvatar(),
            value.brokerageEnabled(),
            value.brokeragePrice(),
            value.frozenPrice(),
            value.firstLevelCount(),
            value.secondLevelCount());
    }

    public static BrokerageRecordRes record(BrokerageRecordDTO value) {
        return new BrokerageRecordRes(
            value.id(),
            value.userId(),
            value.bizId(),
            value.bizType(),
            value.title(),
            value.description(),
            value.price(),
            value.totalPrice(),
            value.status(),
            value.frozenDays(),
            value.unfreezeTime(),
            value.sourceUserLevel(),
            value.createTime());
    }

    public static BrokerageWithdrawRes withdraw(BrokerageWithdrawDTO value) {
        return new BrokerageWithdrawRes(
            value.id(),
            value.userId(),
            value.price(),
            value.feePrice(),
            value.totalPrice(),
            value.type(),
            value.userName(),
            value.userAccount(),
            value.qrCodeUrl(),
            value.bankName(),
            value.bankAddress(),
            value.status(),
            value.auditReason(),
            value.auditTime(),
            value.createTime());
    }
}

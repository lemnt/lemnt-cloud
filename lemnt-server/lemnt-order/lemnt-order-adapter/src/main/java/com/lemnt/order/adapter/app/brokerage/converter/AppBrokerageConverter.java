package com.lemnt.order.adapter.app.brokerage.converter;

import com.lemnt.order.adapter.app.brokerage.req.AppBrokerageBindReq;
import com.lemnt.order.adapter.app.brokerage.req.AppBrokeragePageReq;
import com.lemnt.order.adapter.app.brokerage.req.AppBrokerageWithdrawCreateReq;
import com.lemnt.order.adapter.app.brokerage.res.AppBrokerageRecordRes;
import com.lemnt.order.adapter.app.brokerage.res.AppBrokerageUserRes;
import com.lemnt.order.adapter.app.brokerage.res.AppBrokerageWithdrawRes;
import com.lemnt.order.application.brokerage.cmd.BrokerageBindCmd;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawCreateCmd;
import com.lemnt.order.application.brokerage.dto.BrokerageRecordDTO;
import com.lemnt.order.application.brokerage.dto.BrokerageUserDTO;
import com.lemnt.order.application.brokerage.dto.BrokerageWithdrawDTO;
import com.lemnt.order.application.brokerage.qry.BrokerageRecordPageQry;
import com.lemnt.order.application.brokerage.qry.BrokerageWithdrawPageQry;

/**
 * 会员分销请求及响应转换。
 */
public final class AppBrokerageConverter {
    private AppBrokerageConverter() {
    }

    public static BrokerageBindCmd bind(Long memberId, AppBrokerageBindReq req) {
        return new BrokerageBindCmd(memberId, req.bindUserId());
    }

    public static BrokerageWithdrawCreateCmd withdraw(
        Long memberId, AppBrokerageWithdrawCreateReq req) {
        return new BrokerageWithdrawCreateCmd(
            memberId,
            req.price(),
            req.type(),
            req.userName(),
            req.userAccount(),
            req.qrCodeUrl(),
            req.bankName(),
            req.bankAddress());
    }

    public static BrokerageRecordPageQry records(Long memberId, AppBrokeragePageReq req) {
        return new BrokerageRecordPageQry(
            memberId,
            null,
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static BrokerageWithdrawPageQry withdraws(Long memberId, AppBrokeragePageReq req) {
        return new BrokerageWithdrawPageQry(
            memberId,
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static AppBrokerageUserRes user(BrokerageUserDTO value) {
        return new AppBrokerageUserRes(
            value.id(),
            value.bindUserId(),
            value.bindNickname(),
            value.brokerageEnabled(),
            value.brokeragePrice(),
            value.frozenPrice(),
            value.firstLevelCount(),
            value.secondLevelCount());
    }

    public static AppBrokerageRecordRes record(BrokerageRecordDTO value) {
        return new AppBrokerageRecordRes(
            value.id(),
            value.bizId(),
            value.bizType(),
            value.title(),
            value.description(),
            value.price(),
            value.totalPrice(),
            value.status(),
            value.frozenDays(),
            value.unfreezeTime(),
            value.createTime());
    }

    public static AppBrokerageWithdrawRes withdraw(BrokerageWithdrawDTO value) {
        return new AppBrokerageWithdrawRes(
            value.id(),
            value.price(),
            value.feePrice(),
            value.totalPrice(),
            value.type(),
            value.status(),
            value.auditReason(),
            value.auditTime(),
            value.createTime());
    }
}

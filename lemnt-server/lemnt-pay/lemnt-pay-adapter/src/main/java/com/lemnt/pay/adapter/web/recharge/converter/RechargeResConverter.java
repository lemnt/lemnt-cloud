package com.lemnt.pay.adapter.web.recharge.converter;

import com.lemnt.pay.adapter.web.recharge.req.RechargePackageCreateReq;
import com.lemnt.pay.adapter.web.recharge.req.RechargePackagePageReq;
import com.lemnt.pay.adapter.web.recharge.req.RechargePackageUpdateReq;
import com.lemnt.pay.adapter.web.recharge.req.WalletRechargePageReq;
import com.lemnt.pay.adapter.web.recharge.res.RechargePackageRes;
import com.lemnt.pay.adapter.web.recharge.res.WalletRechargeRes;
import com.lemnt.pay.application.payment.cmd.RechargePackageCreateCmd;
import com.lemnt.pay.application.payment.cmd.RechargePackageUpdateCmd;
import com.lemnt.pay.application.payment.dto.RechargePackageDTO;
import com.lemnt.pay.application.payment.dto.WalletRechargeDTO;
import com.lemnt.pay.application.payment.qry.RechargePackagePageQry;
import com.lemnt.pay.application.payment.qry.WalletRechargePageQry;

/** 充值请求与响应转换。 */
public final class RechargeResConverter {
    private RechargeResConverter() {}

    public static RechargePackageCreateCmd packageCreate(RechargePackageCreateReq req) {
        return new RechargePackageCreateCmd(
                req.name(), req.payPrice(), req.bonusPrice(), req.status());
    }

    public static RechargePackageUpdateCmd packageUpdate(Long id, RechargePackageUpdateReq req) {
        return new RechargePackageUpdateCmd(
                id, req.name(), req.payPrice(), req.bonusPrice(), req.status());
    }

    public static RechargePackagePageQry packagePage(RechargePackagePageReq req) {
        return new RechargePackagePageQry(
                req.name(),
                req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static WalletRechargePageQry rechargePage(WalletRechargePageReq req) {
        return new WalletRechargePageQry(
                req.memberId(),
                req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static RechargePackageRes packageView(RechargePackageDTO value) {
        return value == null
                ? null
                : new RechargePackageRes(
                        value.id(),
                        value.name(),
                        value.payPrice(),
                        value.bonusPrice(),
                        value.status(),
                        value.createTime());
    }

    public static WalletRechargeRes recharge(WalletRechargeDTO value) {
        return value == null
                ? null
                : new WalletRechargeRes(
                        value.id(),
                        value.rechargeNo(),
                        value.requestNo(),
                        value.walletId(),
                        value.memberId(),
                        value.packageId(),
                        value.payPrice(),
                        value.bonusPrice(),
                        value.totalPrice(),
                        value.status(),
                        value.channelCode(),
                        value.paidTime(),
                        value.refundedTime(),
                        value.createTime());
    }
}

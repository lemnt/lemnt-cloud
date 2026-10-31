package com.lemnt.trade.adapter.web.wallet.converter;

import com.lemnt.trade.adapter.web.wallet.req.WalletAdjustReq;
import com.lemnt.trade.adapter.web.wallet.req.WalletPageReq;
import com.lemnt.trade.adapter.web.wallet.req.WalletTransactionPageReq;
import com.lemnt.trade.adapter.web.wallet.res.WalletRes;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransactionRes;
import com.lemnt.trade.application.payment.cmd.WalletAdjustCmd;
import com.lemnt.trade.application.payment.dto.WalletDTO;
import com.lemnt.trade.application.payment.dto.WalletTransactionDTO;
import com.lemnt.trade.application.payment.qry.WalletPageQry;
import com.lemnt.trade.application.payment.qry.WalletTransactionPageQry;

/** 钱包请求与响应转换。 */
public final class WalletResConverter {
    private WalletResConverter() {
    }

    public static WalletPageQry page(WalletPageReq req) {
        return new WalletPageQry(req.memberId(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static WalletTransactionPageQry transactions(WalletTransactionPageReq req) {
        return new WalletTransactionPageQry(req.bizType(), req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    public static WalletAdjustCmd adjust(WalletAdjustReq req) {
        return new WalletAdjustCmd(req.requestNo(), req.amount(), req.reason());
    }

    public static WalletRes wallet(WalletDTO value) {
        return value == null
                ? null
                : new WalletRes(value.id(), value.memberId(), value.balance(), value.freezePrice(),
                        value.totalExpense(), value.totalRecharge(), value.createTime());
    }

    public static WalletTransactionRes transaction(WalletTransactionDTO value) {
        return value == null
                ? null
                : new WalletTransactionRes(value.transactionNo(), value.bizType(), value.bizNo(), value.title(),
                        value.amount(), value.balanceAfter(), value.createTime());
    }
}

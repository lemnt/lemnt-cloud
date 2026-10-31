package com.lemnt.trade.adapter.web.wallet.converter;

import com.lemnt.trade.adapter.web.wallet.req.WalletTransferPageReq;
import com.lemnt.trade.adapter.web.wallet.res.WalletTransferRes;
import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.application.payment.qry.WalletTransferPageQry;

/** 钱包转账请求与响应转换。 */
public final class WalletTransferResConverter {
    private WalletTransferResConverter() {
    }

    public static WalletTransferPageQry page(WalletTransferPageReq req) {
        return new WalletTransferPageQry(null, req.fromMemberId(), req.toMemberId(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }

    public static WalletTransferRes transfer(WalletTransferDTO value) {
        return value == null
                ? null
                : new WalletTransferRes(value.id(), value.transferNo(), value.requestNo(), value.fromWalletId(),
                        value.fromMemberId(), value.toWalletId(), value.toMemberId(), value.amount(), value.remark(),
                        value.status(), value.createTime());
    }
}

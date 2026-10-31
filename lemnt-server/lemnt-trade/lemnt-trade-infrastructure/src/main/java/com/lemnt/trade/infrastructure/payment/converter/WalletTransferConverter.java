package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.WalletTransfer;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransferDO;

/** 钱包转账表字段转换。 */
public final class WalletTransferConverter {
    private WalletTransferConverter() {
    }

    public static WalletTransfer toEntity(WalletTransferDO row) {
        return row == null
                ? null
                : new WalletTransfer(row.getId(), row.getTransferNo(), row.getRequestNo(), row.getFromWalletId(),
                        row.getFromMemberId(), row.getToWalletId(), row.getToMemberId(), row.getAmount(),
                        row.getRemark(), row.getStatus(), row.getCreateTime());
    }

    public static WalletTransferDO toDO(WalletTransfer value) {
        var row = new WalletTransferDO();
        row.setId(value.id());
        row.setTransferNo(value.transferNo());
        row.setRequestNo(value.requestNo());
        row.setFromWalletId(value.fromWalletId());
        row.setFromMemberId(value.fromMemberId());
        row.setToWalletId(value.toWalletId());
        row.setToMemberId(value.toMemberId());
        row.setAmount(value.amount());
        row.setRemark(value.remark());
        row.setStatus(value.status());
        return row;
    }
}

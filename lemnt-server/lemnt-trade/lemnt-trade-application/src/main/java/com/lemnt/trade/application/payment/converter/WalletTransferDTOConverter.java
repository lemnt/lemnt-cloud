package com.lemnt.trade.application.payment.converter;

import com.lemnt.trade.application.payment.dto.WalletTransferDTO;
import com.lemnt.trade.domain.payment.model.WalletTransfer;

/** 钱包转账应用输出转换。 */
public final class WalletTransferDTOConverter {
    private WalletTransferDTOConverter() {
    }

    public static WalletTransferDTO transfer(WalletTransfer value) {
        return value == null
                ? null
                : new WalletTransferDTO(value.id(), value.transferNo(), value.requestNo(), value.fromWalletId(),
                        value.fromMemberId(), value.toWalletId(), value.toMemberId(), value.amount(), value.remark(),
                        value.status(), value.createTime());
    }
}

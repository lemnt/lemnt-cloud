package com.lemnt.trade.application.payment.converter;

import com.lemnt.trade.application.payment.dto.WalletDTO;
import com.lemnt.trade.application.payment.dto.WalletTransactionDTO;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletTransaction;

/** 钱包与流水应用输出转换。 */
public final class WalletDTOConverter {
    private WalletDTOConverter() {
    }

    public static WalletDTO wallet(Wallet value) {
        return value == null
                ? null
                : new WalletDTO(value.id(), value.memberId(), value.balance(), value.freezePrice(),
                        value.totalExpense(), value.totalRecharge(), value.createTime());
    }

    public static WalletTransactionDTO transaction(WalletTransaction value) {
        return value == null
                ? null
                : new WalletTransactionDTO(value.transactionNo(), value.bizType(), value.bizNo(), value.title(),
                        value.amount(), value.balanceAfter(), value.createTime());
    }
}

package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletTransaction;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletDO;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransactionDO;

/** 钱包账户与流水表字段转换。 */
public final class WalletConverter {
    private WalletConverter() {
    }

    public static Wallet toEntity(WalletDO row) {
        return row == null
                ? null
                : new Wallet(row.getId(), row.getMemberId(), row.getBalance(), row.getFreezePrice(),
                        row.getTotalExpense(), row.getTotalRecharge(), row.getCreateTime());
    }

    public static WalletDO toDO(Wallet value) {
        var row = new WalletDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setBalance(value.balance());
        row.setFreezePrice(value.freezePrice());
        row.setTotalExpense(value.totalExpense());
        row.setTotalRecharge(value.totalRecharge());
        return row;
    }

    public static WalletTransaction toEntity(WalletTransactionDO row) {
        return row == null
                ? null
                : new WalletTransaction(row.getId(), row.getTransactionNo(), row.getWalletId(), row.getBizType(),
                        row.getBizNo(), row.getTitle(), row.getAmount(), row.getBalanceAfter(), row.getCreateTime());
    }

    public static WalletTransactionDO toDO(WalletTransaction value) {
        var row = new WalletTransactionDO();
        row.setTransactionNo(value.transactionNo());
        row.setWalletId(value.walletId());
        row.setBizType(value.bizType());
        row.setBizNo(value.bizNo());
        row.setTitle(value.title());
        row.setAmount(value.amount());
        row.setBalanceAfter(value.balanceAfter());
        return row;
    }
}

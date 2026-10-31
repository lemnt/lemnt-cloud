package com.lemnt.trade.application.payment.converter;

import com.lemnt.trade.application.payment.dto.RechargePackageDTO;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.WalletRecharge;

/** 充值套餐与充值单应用输出转换。 */
public final class RechargeDTOConverter {
    private RechargeDTOConverter() {
    }

    public static RechargePackageDTO packageView(RechargePackage value) {
        return value == null
                ? null
                : new RechargePackageDTO(value.id(), value.name(), value.payPrice(), value.bonusPrice(),
                        value.status(), value.createTime());
    }

    public static WalletRechargeDTO recharge(WalletRecharge value) {
        return value == null
                ? null
                : new WalletRechargeDTO(value.id(), value.rechargeNo(), value.requestNo(), value.walletId(),
                        value.memberId(), value.packageId(), value.payPrice(), value.bonusPrice(),
                        value.totalPrice(), value.status(), value.channelCode(), value.paidTime(),
                        value.refundedTime(), value.createTime());
    }
}

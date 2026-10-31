package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.WalletRecharge;
import com.lemnt.trade.infrastructure.payment.dataobject.RechargePackageDO;
import com.lemnt.trade.infrastructure.payment.dataobject.WalletRechargeDO;

/** 充值套餐与充值单表字段转换。 */
public final class RechargeConverter {
    private RechargeConverter() {
    }

    public static RechargePackage toEntity(RechargePackageDO row) {
        return row == null
                ? null
                : new RechargePackage(row.getId(), row.getName(), row.getPayPrice(), row.getBonusPrice(),
                        row.getStatus(), row.getCreateTime());
    }

    public static RechargePackageDO toDO(RechargePackage value) {
        var row = new RechargePackageDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setPayPrice(value.payPrice());
        row.setBonusPrice(value.bonusPrice());
        row.setStatus(value.status());
        return row;
    }

    public static WalletRecharge toEntity(WalletRechargeDO row) {
        return row == null
                ? null
                : new WalletRecharge(row.getId(), row.getRechargeNo(), row.getRequestNo(), row.getWalletId(),
                        row.getMemberId(), row.getPackageId(), row.getPayPrice(), row.getBonusPrice(),
                        row.getTotalPrice(), row.getStatus(), row.getChannelCode(), row.getPaidTime(),
                        row.getRefundedTime(), row.getCreateTime());
    }

    public static WalletRechargeDO toDO(WalletRecharge value) {
        var row = new WalletRechargeDO();
        row.setId(value.id());
        row.setRechargeNo(value.rechargeNo());
        row.setRequestNo(value.requestNo());
        row.setWalletId(value.walletId());
        row.setMemberId(value.memberId());
        row.setPackageId(value.packageId());
        row.setPayPrice(value.payPrice());
        row.setBonusPrice(value.bonusPrice());
        row.setTotalPrice(value.totalPrice());
        row.setStatus(value.status());
        row.setChannelCode(value.channelCode());
        row.setPaidTime(value.paidTime());
        row.setRefundedTime(value.refundedTime());
        return row;
    }
}

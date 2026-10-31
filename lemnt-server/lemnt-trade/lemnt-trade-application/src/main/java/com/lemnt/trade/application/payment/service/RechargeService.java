package com.lemnt.trade.application.payment.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletRecharge;
import com.lemnt.trade.domain.payment.model.WalletRechargeStatus;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/** 充值下单、到账入账与退款；暂无真实渠道，到账由后台确认命令触发。 */
@Component
@RequiredArgsConstructor
public class RechargeService {
    private final RechargePackageGateway packages;
    private final WalletRechargeGateway recharges;
    private final WalletService walletService;

    /** 按套餐创建充值单，requestNo 幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public WalletRecharge create(Long memberId, Long packageId, String requestNo) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        if (packageId == null || packageId <= 0) {
            throw new IllegalArgumentException("套餐编号不合法");
        }
        var value = packages.find(packageId);
        if (value == null || value.status() == null || value.status() != 0) {
            throw new ServiceException("充值套餐不存在或已停用", 400);
        }
        String request = RechargeRules.text(requestNo, 64, "请求编号");
        var existing = recharges.findByRequestNo(request);
        if (existing != null) {
            if (!existing.memberId().equals(memberId) || !existing.packageId().equals(packageId)) {
                throw new IllegalStateException("请求编号已用于不同充值内容");
            }
            return existing;
        }
        var wallet = walletService.getOrCreate(memberId);
        BigDecimal total = value.payPrice().add(value.bonusPrice()).setScale(2);
        var created = new WalletRecharge(null,
                "RC" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT), request,
                wallet.id(), memberId, packageId, value.payPrice(), value.bonusPrice(), total,
                WalletRechargeStatus.PENDING.name(), null, null, null, null);
        return recharges.createIfAbsent(created);
    }

    /** 确认到账：PENDING→PAID 并按充值单号入钱包，重复确认幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public WalletRecharge confirmPaid(String rechargeNo, String channelCode) {
        String no = RechargeRules.text(rechargeNo, 40, "充值单号");
        String channel = RechargeRules.channel(channelCode);
        var current = recharges.lockByNo(no);
        if (current == null) {
            throw new IllegalArgumentException("充值单不存在");
        }
        if (!WalletRechargeStatus.PENDING.name().equals(current.status())) {
            return current;
        }
        var now = recharges.databaseNow();
        var paid = new WalletRecharge(current.id(), current.rechargeNo(), current.requestNo(), current.walletId(),
                current.memberId(), current.packageId(), current.payPrice(), current.bonusPrice(),
                current.totalPrice(), WalletRechargeStatus.PAID.name(), channel, now, null, current.createTime());
        if (!recharges.update(paid)) {
            throw new IllegalStateException("充值单状态已变化");
        }
        walletService.credit(current.memberId(), WalletBizType.RECHARGE, current.rechargeNo(), current.totalPrice(),
                "充值到账");
        return recharges.findByNo(no);
    }

    /** 退款：PAID→REFUNDED 并按充值单号出账，余额不足整体回滚；重复退款幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public WalletRecharge refund(String rechargeNo) {
        String no = RechargeRules.text(rechargeNo, 40, "充值单号");
        var current = recharges.lockByNo(no);
        if (current == null) {
            throw new IllegalArgumentException("充值单不存在");
        }
        if (WalletRechargeStatus.REFUNDED.name().equals(current.status())) {
            return current;
        }
        if (!WalletRechargeStatus.PAID.name().equals(current.status())) {
            throw new ServiceException("充值未到账，不能退款", 400);
        }
        var now = recharges.databaseNow();
        var refunded = new WalletRecharge(current.id(), current.rechargeNo(), current.requestNo(),
                current.walletId(), current.memberId(), current.packageId(), current.payPrice(),
                current.bonusPrice(), current.totalPrice(), WalletRechargeStatus.REFUNDED.name(),
                current.channelCode(), current.paidTime(), now, current.createTime());
        if (!recharges.update(refunded)) {
            throw new IllegalStateException("充值单状态已变化");
        }
        walletService.debit(current.memberId(), WalletBizType.RECHARGE_REFUND, current.rechargeNo(),
                current.totalPrice(), "充值退款");
        return recharges.findByNo(no);
    }
}

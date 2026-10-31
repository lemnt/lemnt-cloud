package com.lemnt.trade.application.payment.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransfer;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/** 钱包间转账：requestNo 幂等、按钱包编号升序加锁、双向流水。收款方需已有钱包。 */
@Component
@RequiredArgsConstructor
public class WalletTransferService {
    private static final String SUCCESS = "SUCCESS";

    private final WalletGateway wallets;
    private final WalletTransferGateway transfers;
    private final WalletService walletService;

    @Transactional(rollbackFor = Exception.class)
    public WalletTransfer transfer(Long fromMemberId, Long toMemberId, BigDecimal amount, String remark,
            String requestNo) {
        requireMember(fromMemberId, "转出会员");
        requireMember(toMemberId, "转入会员");
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("不能向自己转账");
        }
        BigDecimal value = RechargeRules.positiveMoney(amount, "转账金额");
        String request = RechargeRules.text(requestNo, 64, "请求编号");
        String note = remark == null || remark.isBlank() ? null : RechargeRules.text(remark, 128, "转账备注");
        var existing = transfers.findByRequestNo(request);
        if (existing != null) {
            return verify(existing, fromMemberId, toMemberId, value);
        }
        var toWallet = wallets.findByMemberId(toMemberId);
        if (toWallet == null) {
            throw new ServiceException("收款方钱包不存在", 400);
        }
        var fromWallet = walletService.getOrCreate(fromMemberId);
        lockBoth(fromWallet.id(), toWallet.id());
        var created = transfers.createIfAbsent(new WalletTransfer(null,
                "TF" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT), request,
                fromWallet.id(), fromMemberId, toWallet.id(), toMemberId, value, note, SUCCESS, null));
        if (created == null) {
            created = transfers.findByRequestNo(request);
        }
        var record = verify(created, fromMemberId, toMemberId, value);
        walletService.debit(fromMemberId, WalletBizType.TRANSFER, request, value, "转账转出");
        walletService.credit(toMemberId, WalletBizType.TRANSFER, request, value, "转账转入");
        return record;
    }

    private WalletTransfer verify(WalletTransfer value, Long fromMemberId, Long toMemberId, BigDecimal amount) {
        if (!value.fromMemberId().equals(fromMemberId) || !value.toMemberId().equals(toMemberId)
                || value.amount().compareTo(amount) != 0) {
            throw new IllegalStateException("请求编号已用于不同转账内容");
        }
        return value;
    }

    /** 按钱包编号升序加锁，避免 A→B 与 B→A 并发死锁。 */
    private void lockBoth(Long first, Long second) {
        if (first < second) {
            wallets.lockById(first);
            wallets.lockById(second);
        } else {
            wallets.lockById(second);
            wallets.lockById(first);
        }
    }

    private void requireMember(Long memberId, String field) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException(field + "不合法");
        }
    }
}

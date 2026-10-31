package com.lemnt.trade.application.payment.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

/** 钱包资金操作：行锁串行、业务幂等、余额与累计字段维护。 */
@Component
@RequiredArgsConstructor
public class WalletService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    private final WalletGateway wallets;

    /** 读取或创建零余额钱包，App 首次访问使用。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet getOrCreate(Long memberId) {
        requireMember(memberId);
        return wallets.createIfAbsent(memberId);
    }

    /** 入账，写正值流水。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet credit(Long memberId, WalletBizType type, String bizNo, BigDecimal amount, String title) {
        return change(memberId, type, bizNo, amount, title, true);
    }

    /** 出账，余额不足时拒绝并写负值流水。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet debit(Long memberId, WalletBizType type, String bizNo, BigDecimal amount, String title) {
        return change(memberId, type, bizNo, amount, title, false);
    }

    /** 冻结可用余额，不写收支流水。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet freeze(Long memberId, BigDecimal amount) {
        requireMember(memberId);
        BigDecimal value = positive(amount);
        wallets.createIfAbsent(memberId);
        var locked = wallets.lockByMemberId(memberId);
        if (locked.balance().compareTo(value) < 0) {
            throw new ServiceException("钱包可用余额不足", 400);
        }
        var updated = new Wallet(locked.id(), locked.memberId(), locked.balance().subtract(value),
                locked.freezePrice().add(value), locked.totalExpense(), locked.totalRecharge(),
                locked.createTime());
        save(updated);
        return updated;
    }

    /** 解冻回可用余额，不写收支流水。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet unfreeze(Long memberId, BigDecimal amount) {
        requireMember(memberId);
        BigDecimal value = positive(amount);
        wallets.createIfAbsent(memberId);
        var locked = wallets.lockByMemberId(memberId);
        if (locked.freezePrice().compareTo(value) < 0) {
            throw new ServiceException("钱包冻结金额不足", 400);
        }
        var updated = new Wallet(locked.id(), locked.memberId(), locked.balance().add(value),
                locked.freezePrice().subtract(value), locked.totalExpense(), locked.totalRecharge(),
                locked.createTime());
        save(updated);
        return updated;
    }

    /** 人工调整余额：正数入账、负数出账，requestNo 作为幂等业务号。 */
    @Transactional(rollbackFor = Exception.class)
    public Wallet adjust(Long memberId, String requestNo, BigDecimal amount, String reason) {
        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("调整金额不能为0");
        }
        String bizNo = text(requestNo, 64, "请求编号");
        String title = text(reason, 128, "调整原因");
        return amount.signum() > 0
                ? change(memberId, WalletBizType.UPDATE_BALANCE, bizNo, amount, title, true)
                : change(memberId, WalletBizType.UPDATE_BALANCE, bizNo, amount.negate(), title, false);
    }

    private Wallet change(Long memberId, WalletBizType type, String bizNo, BigDecimal amount, String title,
            boolean increase) {
        requireMember(memberId);
        if (type == null) {
            throw new IllegalArgumentException("业务类型不合法");
        }
        BigDecimal value = positive(amount);
        String key = text(bizNo, 64, "业务单号");
        String description = text(title, 128, "流水说明");
        wallets.createIfAbsent(memberId);
        var locked = wallets.lockByMemberId(memberId);
        BigDecimal signed = increase ? value : value.negate();
        var existing = wallets.findTransaction(locked.id(), type.name(), key);
        if (existing != null) {
            if (existing.amount().compareTo(signed) != 0) {
                throw new IllegalStateException("同一业务单号金额不一致");
            }
            return locked;
        }
        BigDecimal balance = locked.balance().add(signed);
        if (balance.signum() < 0) {
            throw new ServiceException("钱包余额不足", 400);
        }
        BigDecimal expense = increase ? locked.totalExpense() : locked.totalExpense().add(value);
        BigDecimal recharge = increase && type == WalletBizType.RECHARGE
                ? locked.totalRecharge().add(value)
                : locked.totalRecharge();
        var updated = new Wallet(locked.id(), locked.memberId(), balance, locked.freezePrice(), expense,
                recharge, locked.createTime());
        save(updated);
        wallets.insertTransaction(new WalletTransaction(null,
                "WT" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT),
                locked.id(), type.name(), key, description, signed, balance, null));
        return updated;
    }

    private void save(Wallet value) {
        if (!wallets.update(value)) {
            throw new IllegalStateException("钱包余额更新冲突");
        }
    }

    private void requireMember(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
    }

    private BigDecimal positive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(MAX_AMOUNT) > 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("金额必须为0至999999999.99之间的两位小数");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private String text(String value, int max, String field) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(field + "不能为空且不超过" + max + "字");
        }
        return value.trim();
    }
}

package com.lemnt.pay.application.payment.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.support.RechargeRules;
import com.lemnt.pay.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.payment.model.WalletBizType;
import com.lemnt.pay.domain.payment.model.WalletRecharge;
import com.lemnt.pay.domain.payment.model.WalletRechargeStatus;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * 充值下单、微信支付与原路退款，钱包与渠道结果在同一资金事务内更新。
 */
@Component
@RequiredArgsConstructor
public class RechargeService {
    // 微信 JSAPI 下单金额以 int 类型的分表示。
    private static final BigDecimal MAX_CUSTOM_WECHAT_AMOUNT =
            BigDecimal.valueOf(Integer.MAX_VALUE, 2);

    private final RechargePackageGateway packages;
    private final WalletRechargeGateway recharges;
    private final WalletService walletService;
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final PayNotifyTaskService tasks;

    /** 锁定本人待支付充值单，用套餐快照的实付金额创建唯一原支付单。 */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord preparePayment(Long memberId, String rechargeNo, String appKey) {
        var recharge = locked(rechargeNo);
        if (!recharge.memberId().equals(memberId)) throw new ServiceException("充值单不存在", 400);
        if (!"PENDING".equals(recharge.status())) throw new ServiceException("充值单无需支付", 400);
        var payment =
                payments.createWaiting(
                        "PAY" + randomNo().substring(0, 24),
                        recharge.rechargeNo(),
                        memberId,
                        "WECHAT",
                        appKey,
                        recharge.payPrice());
        if (!"WAITING".equals(payment.status())) throw new IllegalStateException("原充值支付单已结束");
        return payment;
    }

    /** 仅接管有原充值单的渠道回调，实际收款和钱包入账一起提交。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmChannelPayment(VerifiedPaymentNotification notice) {
        if (recharges.findByNo(notice.orderNo()) == null) return false;
        var recharge = locked(notice.orderNo());
        var payment = payments.lockForConfirmation(notice.orderNo(), notice.paymentNo());
        if (payment == null
                || !"WECHAT".equals(notice.channel())
                || !notice.channel().equals(payment.channel())
                || !recharge.rechargeNo().equals(payment.orderNo())
                || recharge.payPrice().compareTo(notice.amount()) != 0
                || payment.amount().compareTo(notice.amount()) != 0) {
            throw new IllegalArgumentException("充值支付与原单、渠道或实付金额不一致");
        }
        if ("SUCCESS".equals(payment.status())) {
            if (!notice.channelTradeNo().equals(payment.channelTradeNo()))
                throw new IllegalStateException("充值支付渠道单号不一致");
            return true;
        }
        if (!payments.markSuccess(notice)) throw new IllegalStateException("充值状态与渠道结果冲突");
        // 第二条已核验收款仍保留，主单待对账，原充值余额只到账一次。
        var master = payments.findPaymentOrder(recharge.rechargeNo());
        if ("PENDING".equals(recharge.status()) && "SUCCESS".equals(master.status())) {
            credit(recharge, notice.channel());
        }
        tasks.enqueueAppPayment(payment);
        return true;
    }

    /** 微信退款先冻结实付及赠送余额，原退款单存在时直接复用。 */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRefund prepareChannelRefund(String rechargeNo) {
        var recharge = locked(rechargeNo);
        var payment = payments.findByOrderNoAndChannel(recharge.rechargeNo(), "WECHAT");
        if (payment == null) return null;
        var existing = refunds.findByAfterSaleNo(recharge.rechargeNo());
        if (existing != null) return existing;
        var master = payments.findPaymentOrder(recharge.rechargeNo());
        if (master == null
                || !"SUCCESS".equals(master.status())
                || !payment.paymentNo().equals(master.paidPaymentNo())) {
            throw new ServiceException("充值收款待对账，无法确认原退款单", 400);
        }
        if (!"PAID".equals(recharge.status())
                || !"SUCCESS".equals(payment.status())
                || !"WECHAT".equals(recharge.channelCode())
                || payment.amount().compareTo(recharge.payPrice()) != 0) {
            throw new ServiceException("充值未确认到账，不能退款", 400);
        }
        walletService.freeze(recharge.memberId(), recharge.totalPrice());
        var refund =
                new PaymentRefund(
                        "PR" + randomNo(),
                        recharge.rechargeNo(),
                        payment.paymentNo(),
                        recharge.rechargeNo(),
                        payment.channel(),
                        recharge.payPrice(),
                        "WAITING",
                        null);
        refunds.save(refund);
        change(recharge, "REFUNDING", recharge.channelCode(), recharge.paidTime(), null);
        return refund;
    }

    /** 支付服务内处理充值退款终态，失败释放冻结，成功扣回含赠送余额。 */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRefund completeChannelRefund(PaymentRefund original, PaymentRefundResult result) {
        if (recharges.findByNo(original.orderNo()) == null) return null;
        var recharge = locked(original.orderNo());
        var current = refunds.find(original.refundNo());
        if (current == null
                || !recharge.rechargeNo().equals(current.afterSaleNo())
                || !recharge.rechargeNo().equals(current.orderNo())
                || !original.paymentNo().equals(current.paymentNo())
                || !original.channel().equals(current.channel())
                || recharge.payPrice().compareTo(current.amount()) != 0
                || (current.channelRefundNo() != null
                        && result.channelRefundNo() != null
                        && !current.channelRefundNo().equals(result.channelRefundNo()))) {
            throw new IllegalStateException("充值原退款与微信结果不一致");
        }
        if (!"WAITING".equals(current.status())) {
            if ("FAILED".equals(current.status()) && "SUCCESS".equals(result.status())) {
                throw new IllegalStateException("退款失败后收到渠道成功，请核对钱包余额");
            }
            return current;
        }
        if (!"REFUNDING".equals(recharge.status())) throw new IllegalStateException("充值退款冻结状态异常");
        var updated =
                new PaymentRefund(
                        current.refundNo(),
                        current.afterSaleNo(),
                        current.paymentNo(),
                        current.orderNo(),
                        current.channel(),
                        current.amount(),
                        result.status(),
                        result.channelRefundNo() == null
                                ? current.channelRefundNo()
                                : result.channelRefundNo(),
                        current.notifyUrl());
        if (!refunds.updateResult(updated)) throw new IllegalStateException("充值退款写入冲突");
        if (!"WAITING".equals(updated.status())) {
            walletService.unfreeze(recharge.memberId(), recharge.totalPrice());
            if ("SUCCESS".equals(updated.status())) {
                walletService.debit(
                        recharge.memberId(),
                        WalletBizType.RECHARGE_REFUND,
                        recharge.rechargeNo(),
                        recharge.totalPrice(),
                        "充值退款");
                change(
                        recharge,
                        "REFUNDED",
                        recharge.channelCode(),
                        recharge.paidTime(),
                        recharges.databaseNow());
            } else {
                change(recharge, "PAID", recharge.channelCode(), recharge.paidTime(), null);
            }
        }
        tasks.enqueueAppRefund(updated);
        return updated;
    }

    /** 读取已提交的充值状态，用于退单响应。 */
    public WalletRecharge find(String rechargeNo) {
        return recharges.findByNo(rechargeNo);
    }

    private WalletRecharge locked(String rechargeNo) {
        var recharge = recharges.lockByNo(RechargeRules.text(rechargeNo, 40, "充值单号"));
        if (recharge == null) throw new ServiceException("充值单不存在", 400);
        return recharge;
    }

    private String randomNo() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }

    /** 保持套餐充值的调用入口，资金金额由套餐快照决定。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public WalletRecharge create(Long memberId, Long packageId, String requestNo) {
        return create(memberId, packageId, null, requestNo);
    }

    /** 套餐与自定义金额二选一，重复请求按原输入核验。 */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public WalletRecharge create(
            Long memberId, Long packageId, BigDecimal amount, String requestNo) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        if ((packageId == null) == (amount == null) || (packageId != null && packageId <= 0)) {
            throw new IllegalArgumentException("请选择一个充值套餐或填写自定义金额");
        }
        BigDecimal specified = amount == null ? null : RechargeRules.positiveMoney(amount, "充值金额");
        if (specified != null && specified.compareTo(MAX_CUSTOM_WECHAT_AMOUNT) > 0) {
            throw new IllegalArgumentException("充值金额超出微信支付允许范围");
        }
        String request = RechargeRules.text(requestNo, 64, "请求编号");
        var existing = recharges.findByRequestNo(request);
        if (existing != null) return verifyRequest(existing, memberId, packageId, specified);

        BigDecimal payPrice = specified;
        BigDecimal bonusPrice = new BigDecimal("0.00");
        if (packageId != null) {
            var pack = packages.find(packageId);
            if (pack == null || !Integer.valueOf(0).equals(pack.status())) {
                throw new ServiceException("充值套餐不存在或已停用", 400);
            }
            payPrice = pack.payPrice();
            bonusPrice = pack.bonusPrice();
        }
        var wallet = walletService.getOrCreate(memberId);
        var created =
                new WalletRecharge(
                        null,
                        "RC" + randomNo(),
                        request,
                        wallet.id(),
                        memberId,
                        packageId,
                        payPrice,
                        bonusPrice,
                        payPrice.add(bonusPrice),
                        WalletRechargeStatus.PENDING.name(),
                        "WECHAT",
                        null,
                        null,
                        null);
        return verifyRequest(recharges.createIfAbsent(created), memberId, packageId, specified);
    }

    /** 并发创建也须核对原请求内容，避免把其他会员的充值单作为幂等结果返回。 */
    private WalletRecharge verifyRequest(
            WalletRecharge value, Long memberId, Long packageId, BigDecimal specified) {
        if (!memberId.equals(value.memberId())
                || !Objects.equals(packageId, value.packageId())
                || (specified != null && specified.compareTo(value.payPrice()) != 0)) {
            throw new IllegalStateException("请求编号已用于不同充值内容");
        }
        return value;
    }

    /**
     * 确认到账：PENDING→PAID 并按充值单号入钱包，重复确认幂等。
     */
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
        if ("WECHAT".equals(current.channelCode())
                || payments.findByOrderNoAndChannel(no, "WECHAT") != null) {
            throw new ServiceException("微信充值以渠道结果为准", 400);
        }
        credit(current, channel);
        return recharges.findByNo(no);
    }

    private void credit(WalletRecharge current, String channel) {
        change(current, "PAID", channel, recharges.databaseNow(), null);
        walletService.credit(
                current.memberId(),
                WalletBizType.RECHARGE,
                current.rechargeNo(),
                current.totalPrice(),
                "充值到账");
    }

    private void change(
            WalletRecharge current,
            String status,
            String channel,
            LocalDateTime paid,
            LocalDateTime refunded) {
        var updated =
                new WalletRecharge(
                        current.id(),
                        current.rechargeNo(),
                        current.requestNo(),
                        current.walletId(),
                        current.memberId(),
                        current.packageId(),
                        current.payPrice(),
                        current.bonusPrice(),
                        current.totalPrice(),
                        status,
                        channel,
                        paid,
                        refunded,
                        current.createTime());
        if (!recharges.update(updated)) throw new IllegalStateException("充值单状态已变化");
    }

    /**
     * 退款：PAID→REFUNDED 并按充值单号出账，余额不足整体回滚；重复退款幂等。
     */
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
        if (payments.findByOrderNoAndChannel(no, "WECHAT") != null) {
            throw new ServiceException("微信充值必须原路退款", 400);
        }
        change(
                current,
                "REFUNDED",
                current.channelCode(),
                current.paidTime(),
                recharges.databaseNow());
        walletService.debit(
            current.memberId(),
            WalletBizType.RECHARGE_REFUND,
            current.rechargeNo(),
            current.totalPrice(),
            "充值退款");
        return recharges.findByNo(no);
    }
}

package com.lemnt.pay.infrastructure.paymentrefund.channel;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.payment.payment.WechatPayClientProvider;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * 微信支付退款：本地退款单号即渠道幂等键，重试只查询或重放同一笔退款。
 * 未配置退款通知地址，渠道结果由再次发起退款时的查询同步。
 */
@Component
@RequiredArgsConstructor
public class WechatPaymentRefundClient implements PaymentRefundClient {
    private static final String CURRENCY = "CNY";
    private static final int MAX_REASON_LENGTH = 80;
    /**
     * 微信支付对不存在的退款单返回的业务错误码，表示渠道尚未受理。
     */
    private static final Set<String> NOT_FOUND_CODES =
        Set.of("RESOURCE_NOT_EXISTS", "ORDER_NOT_EXIST", "ORDERNOTEXIST");

    private final WechatPayClientProvider clients;

    @Override
    public String channel() {
        return PaymentChannel.WECHAT.name();
    }

    /**
     * 查询原退款单；渠道未受理时返回空，由网关决定是否提交新退款。
     */
    @Override
    public PaymentRefundResult query(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireMatch(refund, payment);
        WechatPayClient client = clients.get(config);
        QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
        request.setOutRefundNo(refund.refundNo());
        try {
            return toResult(client.refund().queryByOutRefundNo(request), refund, payment);
        } catch (ServiceException exception) {
            if (NOT_FOUND_CODES.contains(exception.getErrorCode())) {
                return null;
            }
            throw exception;
        }
    }

    /**
     * 以本地退款单号提交退款；渠道受理后状态为处理中时保留待退款。
     */
    @Override
    public PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireMatch(refund, payment);
        WechatPayClient client = clients.get(config);
        CreateRequest request = new CreateRequest();
        request.setOutTradeNo(payment.paymentNo());
        request.setOutRefundNo(refund.refundNo());
        request.setReason(reason(refund));
        AmountReq amount = new AmountReq();
        amount.setCurrency(CURRENCY);
        amount.setRefund(cents(refund.amount()));
        amount.setTotal(cents(payment.amount()));
        request.setAmount(amount);
        return toResult(client.refund().create(request), refund, payment);
    }

    /**
     * 核验渠道退款单号、原支付单号与金额后映射状态；金额不一致必须失败。
     */
    private PaymentRefundResult toResult(
        Refund value, PaymentRefund refund, PaymentRecord payment) {
        if (value == null) {
            throw new IllegalStateException("微信退款未返回结果");
        }
        if (!refund.refundNo().equals(value.getOutRefundNo())) {
            throw new IllegalStateException("微信退款单号与本地退款单不一致");
        }
        if (value.getOutTradeNo() != null && !payment.paymentNo().equals(value.getOutTradeNo())) {
            throw new IllegalStateException("微信退款绑定的原支付单不一致");
        }
        var amount = value.getAmount();
        if (amount == null) {
            throw new IllegalStateException("微信退款金额缺失");
        }
        BigDecimal refunded = yuan(amount.getRefund());
        if (refunded.compareTo(refund.amount()) != 0) {
            throw new IllegalStateException("微信退款金额与本地退款单不一致");
        }
        return new PaymentRefundResult(
            refund.refundNo(),
            refund.paymentNo(),
            channel(),
            refunded,
            status(value.getStatus()),
            value.getRefundId());
    }

    /**
     * 渠道退款状态映射到本地资金状态；异常单需人工核对，不当作成功。
     */
    private String status(Status status) {
        if (status == null) {
            return "WAITING";
        }
        return switch (status) {
            case SUCCESS -> "SUCCESS";
            case PROCESSING -> "WAITING";
            case CLOSED, ABNORMAL -> "FAILED";
        };
    }

    private BigDecimal yuan(Long fen) {
        if (fen == null || fen <= 0) {
            throw new IllegalStateException("微信退款金额不合法");
        }
        return BigDecimal.valueOf(fen).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
    }

    private long cents(BigDecimal amount) {
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalStateException("退款金额精度不合法");
        }
    }

    private String reason(PaymentRefund refund) {
        String value = "售后" + refund.afterSaleNo();
        return value.length() <= MAX_REASON_LENGTH
            ? value
            : value.substring(0, MAX_REASON_LENGTH);
    }

    private void requireMatch(PaymentRefund refund, PaymentRecord payment) {
        if (refund == null
            || payment == null
            || !PaymentChannel.WECHAT.name().equals(refund.channel())
            || !PaymentChannel.WECHAT.name().equals(payment.channel())
            || refund.paymentNo() == null
            || !refund.paymentNo().equals(payment.paymentNo())
            || refund.amount() == null
            || payment.amount() == null
            || refund.amount().compareTo(payment.amount()) > 0
            || !"SUCCESS".equals(payment.status())
            || payment.channelTradeNo() == null
            || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("微信退款与原支付不一致");
        }
    }
}

package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.cmd.PaymentPrepayCmd;
import com.lemnt.pay.application.payment.dto.PaymentPrepayDTO;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.payment.gateway.PayAppGateway;
import com.lemnt.pay.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.payment.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.payment.model.PayApp;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

/** 会员发起订单支付：校验订单归属后创建待支付单，再交由渠道生成预支付参数。 */
@Component
@RequiredArgsConstructor
public class PaymentPrepayCmdExe {
    private final OrderPaymentGateway orders;
    private final PayAppGateway apps;
    private final PayChannelConfigGateway channels;
    private final TradePaymentCreateCmdExe payments;
    private final PaymentChannelGateway channelGateway;

    public PaymentPrepayDTO execute(PaymentPrepayCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        require(
                cmd.orderNo() != null && !cmd.orderNo().isBlank() && cmd.orderNo().length() <= 40,
                "订单编号不合法");
        require(
                cmd.appKey() != null && !cmd.appKey().isBlank() && cmd.appKey().length() <= 64,
                "支付应用不能为空");
        String channel = channel(cmd.channel());
        var order = orders.findByOrderNo(cmd.orderNo());
        if (order == null || !cmd.memberId().equals(order.memberId())) {
            throw new ServiceException("订单不存在", 400);
        }
        require("PENDING_PAYMENT".equals(order.status()), "只有待支付订单可以发起支付");
        require(order.totalAmount() != null && order.totalAmount().signum() > 0, "订单无需支付");
        PayApp app = apps.findByAppKey(cmd.appKey());
        require(app != null && Integer.valueOf(0).equals(app.status()), "支付应用不存在或已停用");
        PayChannelConfig config =
                channels.listByAppId(app.id()).stream()
                        .filter(
                                value ->
                                        channel.equals(value.code())
                                                && Integer.valueOf(0).equals(value.status()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("支付渠道未启用", 400));
        PaymentRecord payment = payments.execute(order.orderNo(), channel, app.appKey());
        require(payment.appKey() == null || app.appKey().equals(payment.appKey()), "支付单已绑定其他应用");
        require("WAITING".equals(payment.status()), "支付单已关闭或已支付");
        PaymentPrepay prepay = channelGateway.prepay(payment, config);
        return new PaymentPrepayDTO(
                prepay.paymentNo(),
                order.orderNo(),
                prepay.channel(),
                payment.amount(),
                prepay.payUrl(),
                prepay.params());
    }

    /** 渠道编码只接受系统声明支持的范围。 */
    private String channel(String value) {
        try {
            return PaymentChannel.valueOf(
                            value == null ? "" : value.trim().toUpperCase(Locale.ROOT))
                    .name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("暂不支持该支付渠道", 400);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}

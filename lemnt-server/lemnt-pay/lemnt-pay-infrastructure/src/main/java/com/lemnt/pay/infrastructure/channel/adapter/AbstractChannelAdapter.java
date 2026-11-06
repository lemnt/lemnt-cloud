package com.lemnt.pay.infrastructure.channel.adapter;

import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

/**
 * 支付渠道统一能力：下单、回调验签、关单与退款都由同一声道实现承接。
 * 渠道 SDK、签名与金额换算封装在实现类内部，调用方只传业务单号与配置。
 */
public interface AbstractChannelAdapter {

    /**
     * 渠道编码。
     */
    String channel();

    /**
     * 发起支付，返回客户端调起支付所需参数。
     *
     * @param payment     本地支付单
     * @param config      渠道配置
     * @param payerOpenId 渠道要求的付款人标识，为空时由渠道退回扫码等无需标识的方式
     */
    PaymentPrepay payment(PaymentRecord payment, PayChannelConfig config, String payerOpenId);

    /**
     * 验签并解密支付回调；验签失败必须抛错，不得返回已核验对象。
     */
    VerifiedPaymentNotification paymentCallback(
        PayChannelConfig config, PaymentChannelNotification notification);

    /**
     * 关闭未支付订单；只有渠道确认关闭或订单已不存在才返回 true。
     */
    boolean close(PaymentRecord payment, PayChannelConfig config);

    /**
     * 发起退款，以本地退款单号作为渠道幂等键。
     */
    PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config);

    /**
     * 查询原退款单，渠道尚未受理时返回 null。
     */
    PaymentRefundResult refundQuery(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config);
}

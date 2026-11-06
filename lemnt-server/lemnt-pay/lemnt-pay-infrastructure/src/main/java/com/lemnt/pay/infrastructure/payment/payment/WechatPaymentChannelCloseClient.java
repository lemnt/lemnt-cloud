package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 微信支付关单：只有渠道确认已关闭或订单不存在才算关闭成功，未知结果一律抛出等待重试。
 */
@Component
@RequiredArgsConstructor
public class WechatPaymentChannelCloseClient implements PaymentChannelCloseClient {
    /**
     * 微信支付对已关闭、已不存在的订单返回的业务错误码，均表示没有可关闭的在途交易。
     */
    private static final Set<String> CLOSED_CODES =
        Set.of("ORDER_CLOSED", "ORDERCLOSED", "ORDER_NOT_EXIST", "ORDERNOTEXIST");

    private final WechatPayClientProvider clients;

    @Override
    public String channel() {
        return PaymentChannel.WECHAT.name();
    }

    @Override
    public boolean closeUnpaid(PaymentRecord payment, PayChannelConfig config) {
        WechatPayClient client = clients.get(config);
        CloseOrderRequest request = new CloseOrderRequest();
        request.setMchid(client.mchId());
        request.setOutTradeNo(payment.paymentNo());
        try {
            client.nativePay().closeOrder(request);
            return true;
        } catch (ServiceException exception) {
            return CLOSED_CODES.contains(exception.getErrorCode());
        }
    }
}

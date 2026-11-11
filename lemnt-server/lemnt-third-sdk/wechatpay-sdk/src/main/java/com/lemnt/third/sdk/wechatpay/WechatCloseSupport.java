package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;

import java.util.Set;

/**
 * 微信关单：只有渠道确认已关闭或订单不存在才返回成功，未知结果一律视为未关闭。
 */
final class WechatCloseSupport {
    /**
     * 微信对已关闭、已不存在的订单返回的业务错误码，均表示没有可关闭的在途交易。
     */
    private static final Set<String> CLOSED_CODES =
        Set.of("ORDER_CLOSED", "ORDERCLOSED", "ORDER_NOT_EXIST", "ORDERNOTEXIST");

    private WechatCloseSupport() {
    }

    static boolean close(NativePayService service, String outTradeNo, String mchId) {
        WechatPrepaySupport.requireText(outTradeNo, 32, "商户支付单号");
        CloseOrderRequest request = new CloseOrderRequest();
        request.setMchid(mchId);
        request.setOutTradeNo(outTradeNo);
        try {
            service.closeOrder(request);
            return true;
        } catch (ServiceException exception) {
            return CLOSED_CODES.contains(exception.getErrorCode());
        }
    }
}

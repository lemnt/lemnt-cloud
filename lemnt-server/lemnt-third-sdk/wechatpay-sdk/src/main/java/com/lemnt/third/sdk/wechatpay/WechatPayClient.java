package com.lemnt.third.sdk.wechatpay;

import com.lemnt.third.sdk.wechatpay.config.WechatPayConfig;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;

/**
 * 微信支付 APIv3 客户端：按渠道配置装配官方 SDK 的签名、验签和平台证书组件。
 * 不承载业务判断，调用方自行校验商户、金额和业务单号的一致性。
 */
public class WechatPayClient {

    private final WechatPayConfig channelConfig;
    private final NotificationParser notificationParser;
    private final NativePayService nativePayService;
    private final JsapiServiceExtension jsapiService;
    private final RefundService refundService;

    private WechatPayClient(WechatPayConfig channelConfig) {
        channelConfig.requireValid();
        RSAAutoCertificateConfig rsaConfig =
            new RSAAutoCertificateConfig.Builder()
                .merchantId(channelConfig.getMchId())
                .privateKey(channelConfig.getPrivateKey())
                .merchantSerialNumber(channelConfig.getSerialNo())
                .apiV3Key(channelConfig.getApiV3key())
                .build();
        this.channelConfig = channelConfig;
        this.notificationParser = new NotificationParser(rsaConfig);
        this.nativePayService = new NativePayService.Builder().config(rsaConfig).build();
        this.jsapiService = new JsapiServiceExtension.Builder().config(rsaConfig).build();
        this.refundService = new RefundService.Builder().config(rsaConfig).build();
    }

    /**
     * 创建客户端；官方 SDK 会在创建时下载并缓存微信支付平台证书，属于网络操作。
     *
     * @param channelConfig 渠道配置
     * @return 客户端
     */
    public static WechatPayClient of(WechatPayConfig channelConfig) {
        if (channelConfig == null) {
            throw new IllegalArgumentException("微信支付配置不能为空");
        }
        return new WechatPayClient(channelConfig);
    }

    /**
     * 商户应用标识，预支付请求与通知核验使用。
     */
    public String appId() {
        return channelConfig.getAppId();
    }

    /**
     * 商户号，预支付请求与通知核验使用。
     */
    public String mchId() {
        return channelConfig.getMchId();
    }

    /**
     * 支付结果通知地址，创建预支付单时提交给微信支付。
     */
    public String payNotifyUrl() {
        return channelConfig.getPayNotifyUrl();
    }

    /**
     * Native 支付服务：预支付、按商户单号查询、关闭未支付订单。
     */
    public NativePayService nativePay() {
        return nativePayService;
    }

    /**
     * JSAPI 支付服务：小程序内调起支付，返回已签名的支付参数。
     */
    public JsapiServiceExtension jsapi() {
        return jsapiService;
    }

    /**
     * 退款服务：以原退款单号为幂等键发起退款、查询退款结果。
     */
    public RefundService refund() {
        return refundService;
    }

    /**
     * 验签并解密支付通知，返回渠道交易事实。
     *
     * @param notify 回调请求头与报文
     * @return 交易信息
     */
    public Transaction parseTransactionNotification(WechatPayNotifyRequest notify) {
        RequestParam requestParam =
            new RequestParam.Builder()
                .serialNumber(notify.serialNumber())
                .signature(notify.signature())
                .timestamp(notify.timestamp())
                .nonce(notify.nonce())
                .signType(notify.signType())
                .body(notify.body())
                .build();
        return notificationParser.parse(requestParam, Transaction.class);
    }
}

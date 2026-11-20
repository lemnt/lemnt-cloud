package com.lemnt.third.sdk.wechatpay;

import com.lemnt.third.sdk.wechatpay.config.WechatPayConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.RefundNotification;

/**
 * 微信支付 APIv3 客户端：对外只接收业务入参，微信字段、金额换算、签名与验签细节封装在 SDK 内。
 * 不承载业务判断，调用方自行校验单号、金额归属等业务事实。
 */
public class WechatPayClient {

    private final WechatPayConfig channelConfig;
    private final NotificationParser notificationParser;
    private final NativePayService nativePayService;
    private final JsapiServiceExtension jsapiService;
    private final RefundService refundService;

    private WechatPayClient(WechatPayConfig channelConfig) {
        channelConfig.requireValid();
        RSAPublicKeyConfig rsaConfig =
                new RSAPublicKeyConfig.Builder()
                        .merchantId(channelConfig.getMchId())
                        .privateKey(channelConfig.getPrivateKey())
                        .merchantSerialNumber(channelConfig.getSerialNo())
                        .publicKey(channelConfig.getPublicKey())
                        .publicKeyId(channelConfig.getPublicKeyId())
                        .apiV3Key(channelConfig.getApiV3key())
                        .build();
        this.channelConfig = channelConfig;
        this.notificationParser = new NotificationParser(rsaConfig);
        this.nativePayService = new NativePayService.Builder().config(rsaConfig).build();
        this.jsapiService = new JsapiServiceExtension.Builder().config(rsaConfig).build();
        this.refundService = new RefundService.Builder().config(rsaConfig).build();
    }

    /**
     * 使用渠道配置中的微信支付公钥创建客户端，请求应答和回调共用验签配置。
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
     * 商户应用标识，通知核验使用。
     */
    public String appId() {
        return channelConfig.getAppId();
    }

    /**
     * 商户号，通知核验使用。
     */
    public String mchId() {
        return channelConfig.getMchId();
    }

    /**
     * 支付结果通知地址，由调用方填入预支付参数。
     */
    public String payNotifyUrl() {
        return channelConfig.getPayNotifyUrl();
    }

    /**
     * 下单：带付款人 openid 时使用小程序 JSAPI，否则返回扫码支付参数。
     *
     * @param command 本地单号、订单号、金额、通知地址与可选 openid
     * @return 预支付方式与调起支付参数
     */
    public WechatPrepayResult prepay(WechatPrepayCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("微信预支付参数不能为空");
        }
        return command.openId() == null || command.openId().isBlank()
            ? WechatNativePrepay.create(nativePayService, command, appId(), mchId())
            : WechatJsapiPrepay.create(jsapiService, command, appId(), mchId());
    }

    /**
     * 以本地退款单号作为渠道幂等键发起退款。
     */
    public WechatRefundResult refund(WechatRefundCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("微信退款参数不能为空");
        }
        return WechatRefundSupport.create(refundService, command, channelConfig.getRefundNotifyUrl());
    }

    /** 按原商户支付单号查单，渠道确认不存在时返回空。 */
    public WechatTransaction queryPayment(String outTradeNo) {
        WechatPrepaySupport.requireText(outTradeNo, 32, "商户支付单号");
        var request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(mchId());
        request.setOutTradeNo(outTradeNo);
        try {
            return WechatNotificationMapper.toTransaction(nativePayService.queryOrderByOutTradeNo(request));
        } catch (ServiceException error) {
            if ("ORDER_NOT_EXIST".equals(error.getErrorCode()) || "ORDERNOTEXIST".equals(error.getErrorCode())) {
                return null;
            }
            throw error;
        }
    }

    /** 验签并解密原路退款通知。 */
    public WechatRefundResult parseRefundNotification(WechatPayNotifyRequest notify) {
        return WechatRefundSupport.notification(notificationParser.parse(requestParam(notify), RefundNotification.class));
    }

    /**
     * 查询原退款单，渠道尚未受理时返回 null。
     */
    public WechatRefundResult queryRefund(String outRefundNo) {
        return WechatRefundSupport.query(refundService, outRefundNo);
    }

    /**
     * 关闭未支付订单；true 表示渠道已确认关闭或订单不存在。
     */
    public boolean closeOrder(String outTradeNo) {
        return WechatCloseSupport.close(nativePayService, outTradeNo, mchId());
    }

    /**
     * 验签并解密支付通知，返回渠道交易事实。
     *
     * @param notify 回调请求头与报文
     * @return 交易信息
     */
    public WechatTransaction parseTransactionNotification(WechatPayNotifyRequest notify) {
        return WechatNotificationMapper.toTransaction(
            notificationParser.parse(requestParam(notify), Transaction.class));
    }

    private RequestParam requestParam(WechatPayNotifyRequest notify) {
        if (notify == null) {
            throw new IllegalArgumentException("微信支付通知不能为空");
        }
        return
            new RequestParam.Builder()
                .serialNumber(notify.serialNumber())
                .signature(notify.signature())
                .timestamp(notify.timestamp())
                .nonce(notify.nonce())
                .signType(notify.signType())
                .body(notify.body())
                .build();
    }
}

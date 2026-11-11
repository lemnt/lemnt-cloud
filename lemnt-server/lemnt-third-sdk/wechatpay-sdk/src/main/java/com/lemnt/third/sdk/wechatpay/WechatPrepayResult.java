package com.lemnt.third.sdk.wechatpay;

import java.io.Serializable;
import java.util.Map;

/**
 * 预支付结果：params 直接交给客户端调起支付，不含渠道密钥。
 */
public record WechatPrepayResult(WechatPrepayType type, Map<String, String> params)
    implements Serializable {

    public WechatPrepayResult {
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}

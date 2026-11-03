package com.lemnt.pay.adapter.app.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 预支付结果，客户端凭 payUrl 或 params 发起渠道支付。 */
@Schema(description = "预支付结果")
public record AppPaymentPrepayRes(
        @Schema(description = "支付单号") String paymentNo,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "支付渠道") String channel,
        @Schema(description = "支付金额，人民币元") BigDecimal amount,
        @Schema(description = "渠道支付地址，沙箱渠道为空") String payUrl,
        @Schema(description = "渠道支付参数，不含渠道密钥") Map<String, String> params)
        implements Serializable {
    public AppPaymentPrepayRes {
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}

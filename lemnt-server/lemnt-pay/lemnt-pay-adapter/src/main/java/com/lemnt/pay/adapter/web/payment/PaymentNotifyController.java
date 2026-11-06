package com.lemnt.pay.adapter.web.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.pay.application.payment.cmd.PaymentNotifyReceiveCmd;
import com.lemnt.pay.application.payment.executor.PaymentNotifyReceiveCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 支付渠道异步通知入口，验签通过后才提交支付结果。
 */
@Tag(name = "支付渠道通知")
@RestController
@RequestMapping("/trade/payment/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {
    private static final String GENERIC_SIGNATURE = "X-Pay-Signature";
    private static final String GENERIC_TIMESTAMP = "X-Pay-Timestamp";
    private static final String GENERIC_NONCE = "X-Pay-Nonce";
    private static final String GENERIC_SERIAL = "X-Pay-Serial";
    private static final String GENERIC_SIGN_TYPE = "X-Pay-Sign-Type";
    private static final String WECHAT_SIGNATURE = "Wechatpay-Signature";
    private static final String WECHAT_TIMESTAMP = "Wechatpay-Timestamp";
    private static final String WECHAT_NONCE = "Wechatpay-Nonce";
    private static final String WECHAT_SERIAL = "Wechatpay-Serial";
    private static final String WECHAT_SIGN_TYPE = "Wechatpay-Signature-Type";

    private final PaymentNotifyReceiveCmdExe receive;

    @PostMapping("/{appKey}/{channel}")
    @Operation(summary = "接收渠道支付通知")
    public R<String> notify(
        @PathVariable String appKey,
        @PathVariable String channel,
        @RequestHeader HttpHeaders headers,
        @RequestBody String body) {
        return R.ok(
            receive.execute(
                new PaymentNotifyReceiveCmd(
                    appKey,
                    channel,
                    body,
                    header(headers, GENERIC_SIGNATURE, WECHAT_SIGNATURE),
                    header(headers, GENERIC_TIMESTAMP, WECHAT_TIMESTAMP),
                    header(headers, GENERIC_NONCE, WECHAT_NONCE),
                    header(headers, GENERIC_SERIAL, WECHAT_SERIAL),
                    header(headers, GENERIC_SIGN_TYPE, WECHAT_SIGN_TYPE))));
    }

    /**
     * 优先取通用验签头，其次取微信支付原生请求头；两者都没有时由渠道适配器明确拒绝。
     */
    private String header(HttpHeaders headers, String generic, String wechat) {
        String value = headers.getFirst(generic);
        return value != null ? value : headers.getFirst(wechat);
    }
}

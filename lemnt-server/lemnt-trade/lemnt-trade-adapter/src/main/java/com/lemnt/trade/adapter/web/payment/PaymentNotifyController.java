package com.lemnt.trade.adapter.web.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.application.payment.cmd.PaymentNotifyReceiveCmd;
import com.lemnt.trade.application.payment.executor.PaymentNotifyReceiveCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 支付渠道异步通知入口，验签通过后才提交支付结果。 */
@Tag(name = "支付渠道通知")
@RestController
@RequestMapping("/trade/payment/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {
    private final PaymentNotifyReceiveCmdExe receive;

    @PostMapping("/{appKey}/{channel}")
    @Operation(summary = "接收渠道支付通知")
    public R<String> notify(@PathVariable String appKey, @PathVariable String channel,
            @RequestHeader(value = "X-Pay-Signature", required = false) String signature,
            @RequestHeader(value = "X-Pay-Timestamp", required = false) String timestamp,
            @RequestBody String body) {
        return R.ok(receive.execute(new PaymentNotifyReceiveCmd(appKey, channel, body, signature, timestamp)));
    }
}

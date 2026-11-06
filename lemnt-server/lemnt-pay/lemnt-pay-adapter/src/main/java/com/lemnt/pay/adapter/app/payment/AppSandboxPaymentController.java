package com.lemnt.pay.adapter.app.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.pay.application.payment.executor.SandboxPaymentCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 沙箱收银台支付确认，仅在 payment.sandbox.enabled=true 时可用。
 */
@Tag(name = "沙箱收银台")
@RestController
@RequestMapping("/trade/app/payments")
@RequiredArgsConstructor
public class AppSandboxPaymentController {
    private final ObjectProvider<SandboxPaymentCmdExe> sandboxPayment;

    @PostMapping("/{paymentNo}/sandbox-pay")
    @Operation(summary = "本人沙箱支付单确认支付")
    public R<String> pay(@PathVariable String paymentNo) {
        var executor = sandboxPayment.getIfAvailable();
        if (executor == null) {
            return R.fail("沙箱支付未启用");
        }
        return R.ok(executor.execute(SecurityUtils.getUserId(), paymentNo));
    }
}

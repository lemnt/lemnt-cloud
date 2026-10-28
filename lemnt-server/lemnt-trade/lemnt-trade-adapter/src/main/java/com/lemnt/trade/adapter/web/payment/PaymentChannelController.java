package com.lemnt.trade.adapter.web.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.payment.converter.PaymentChannelResConverter;
import com.lemnt.trade.adapter.web.payment.res.PaymentChannelRes;
import com.lemnt.trade.application.payment.executor.PaymentChannelListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 后台支付配置展示接口。 */
@Tag(name = "支付配置")
@RestController
@RequestMapping("/trade/admin/payment-channels")
@RequiredArgsConstructor
public class PaymentChannelController {
    private final PaymentChannelListQueryExe query;

    /** 展示支持的支付渠道，不包含商户密钥或接入状态。 */
    @GetMapping
    @Operation(summary = "查询支持的支付渠道")
    @RequiresPermissions("trade:payment-channel:list")
    public R<List<PaymentChannelRes>> list() {
        return R.ok(query.execute().stream().map(PaymentChannelResConverter::toRes).toList());
    }
}

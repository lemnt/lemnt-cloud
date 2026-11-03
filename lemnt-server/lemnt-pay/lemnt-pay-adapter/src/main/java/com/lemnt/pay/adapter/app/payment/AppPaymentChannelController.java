package com.lemnt.pay.adapter.app.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.pay.adapter.app.payment.res.AppPaymentChannelRes;
import com.lemnt.pay.application.payment.executor.MemberPaymentChannelQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序收银台渠道查询。 */
@Tag(name = "小程序收银台")
@RestController
@RequestMapping("/trade/app/payment-channels")
@RequiredArgsConstructor
public class AppPaymentChannelController {
    private final MemberPaymentChannelQueryExe channels;

    @GetMapping
    @Operation(summary = "查询支付应用下已启用的渠道")
    public R<List<AppPaymentChannelRes>> list(@RequestParam String appKey) {
        return R.ok(
                channels.execute(appKey).stream()
                        .map(value -> new AppPaymentChannelRes(value.code(), value.name()))
                        .toList());
    }
}

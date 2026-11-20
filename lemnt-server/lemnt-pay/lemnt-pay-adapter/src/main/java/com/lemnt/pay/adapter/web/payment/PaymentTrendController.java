package com.lemnt.pay.adapter.web.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.pay.adapter.web.payment.req.PaymentTrendReq;
import com.lemnt.pay.adapter.web.payment.res.PaymentTrendDayRes;
import com.lemnt.pay.application.payment.executor.PaymentTrendQueryExe;
import com.lemnt.pay.application.payment.qry.PaymentTrendQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "支付到账趋势")
@RestController
@RequestMapping("/trade/admin/payments/trend")
@RequiredArgsConstructor
public class PaymentTrendController {
    private final PaymentTrendQueryExe query;

    /** 同时读取支付与充值汇总，要求两项列表权限。 */
    @GetMapping
    @Operation(summary = "查询订单支付与充值到账日趋势")
    @RequiresPermissions({"trade:payment:list", "trade:wallet-recharge:list"})
    public R<List<PaymentTrendDayRes>> trend(@Valid @ModelAttribute PaymentTrendReq req) {
        return R.ok(
                query.execute(new PaymentTrendQry(req.from(), req.to())).stream()
                        .map(
                                day ->
                                        new PaymentTrendDayRes(
                                                day.date().toString(),
                                                day.payments(),
                                                day.paid().setScale(2).toPlainString(),
                                                day.recharged().setScale(2).toPlainString()))
                        .toList());
    }
}

package com.lemnt.pay.adapter.app.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.pay.adapter.app.payment.converter.MemberPaymentStatusConverter;
import com.lemnt.pay.adapter.app.payment.req.AppPaymentCreateReq;
import com.lemnt.pay.adapter.app.payment.res.AppPaymentPrepayRes;
import com.lemnt.pay.adapter.app.payment.res.MemberPaymentStatusRes;
import com.lemnt.pay.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.pay.application.payment.executor.PaymentPrepayCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 小程序本地支付记录查询。
 */
@Tag(name = "小程序本地支付记录查询")
@RestController
@RequestMapping("/trade/app/orders")
@RequiredArgsConstructor
public class AppPaymentController {
    private final MemberPaymentStatusQueryExe memberPaymentStatusQueryExe;
    private final PaymentPrepayCmdExe paymentPrepayCmdExe;

    @GetMapping("/{orderNo}/payment")
    @Operation(summary = "查询本人订单本地支付状态，当前不核验真实支付渠道")
    public R<MemberPaymentStatusRes> status(@PathVariable String orderNo) {
        var query = MemberPaymentStatusConverter.query(SecurityUtils.getUserId(), orderNo);
        var value = memberPaymentStatusQueryExe.execute(query);
        return value == null ? R.fail("订单不存在") : R.ok(MemberPaymentStatusConverter.response(value));
    }

    @PostMapping("/{orderNo}/payment")
    @Operation(summary = "发起本人订单支付，金额由服务端订单确定")
    public R<AppPaymentPrepayRes> prepay(
        @PathVariable String orderNo, @Valid @RequestBody AppPaymentCreateReq req) {
        return R.ok(
            MemberPaymentStatusConverter.prepay(
                paymentPrepayCmdExe.execute(
                    MemberPaymentStatusConverter.prepay(
                        SecurityUtils.getUserId(), orderNo, req))));
    }
}

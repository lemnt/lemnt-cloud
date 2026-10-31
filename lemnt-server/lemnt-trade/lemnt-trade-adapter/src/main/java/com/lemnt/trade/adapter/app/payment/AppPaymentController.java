package com.lemnt.trade.adapter.app.payment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.adapter.app.payment.converter.MemberPaymentStatusConverter;
import com.lemnt.trade.adapter.app.payment.res.MemberPaymentStatusRes;
import com.lemnt.trade.application.payment.executor.MemberPaymentStatusQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 小程序本地支付记录查询。
 */
@Tag(name = "小程序本地支付记录查询")
@RestController
@RequestMapping("/trade/app/orders")
@RequiredArgsConstructor
public class AppPaymentController {
    private final MemberPaymentStatusQueryExe memberPaymentStatusQueryExe;

    @GetMapping("/{orderNo}/payment")
    @Operation(summary = "查询本人订单本地支付状态，当前不核验真实支付渠道")
    public R<MemberPaymentStatusRes> status(
            @PathVariable String orderNo) {
        var query = MemberPaymentStatusConverter.query(SecurityUtils.getUserId(), orderNo);
        var value = memberPaymentStatusQueryExe.execute(query);
        return value == null ? R.fail("订单不存在") : R.ok(MemberPaymentStatusConverter.response(value));
    }
}

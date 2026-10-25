package com.lemnt.trade.adapter.web.app;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.application.checkout.service.MemberSessionService;
import com.lemnt.trade.application.checkout.executor.MemberPaymentStatusQueryExe;
import com.lemnt.trade.adapter.web.app.res.MemberPaymentStatusRes;
import com.lemnt.trade.adapter.web.app.converter.MemberPaymentStatusConverter;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "小程序本地支付记录查询")
@RestController
@RequestMapping("/trade/app/orders")
@RequiredArgsConstructor
public class AppPaymentController {
    private final MemberSessionService sessions;
    private final MemberPaymentStatusQueryExe status;

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    public R<Void> unauthorized() {
        return R.fail("会员登录已失效");
    }

    @GetMapping("/{orderNo}/payment")
    @Operation(summary = "查询本人订单本地支付状态，当前不核验真实支付渠道")
    public R<MemberPaymentStatusRes> status(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String orderNo) {
        var query = MemberPaymentStatusConverter.query(sessions.requireMember(token), orderNo);
        var value = status.execute(query);
        return value == null ? R.fail("订单不存在") : R.ok(MemberPaymentStatusConverter.response(value));
    }
}

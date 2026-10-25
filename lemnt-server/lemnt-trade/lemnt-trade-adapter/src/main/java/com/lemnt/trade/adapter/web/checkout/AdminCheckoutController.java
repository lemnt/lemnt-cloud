package com.lemnt.trade.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.checkout.req.*;
import com.lemnt.trade.adapter.web.checkout.res.*;
import com.lemnt.trade.adapter.web.checkout.converter.*;
import com.lemnt.trade.application.checkout.executor.*;
import com.lemnt.trade.application.checkout.cmd.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "后台购物车结算")
@RestController
@RequestMapping("/trade/admin")
@RequiredArgsConstructor
public class AdminCheckoutController {
    private final CheckoutQuoteQueryExe quote;
    private final CheckoutSubmitCmdExe checkout;
    private final CheckoutReceiptQueryExe receipt;

    @Operation(summary = "服务端重新计价的结算预览，不锁券也不预占库存")
    @PostMapping("/members/{memberId}/checkout/quote")
    @RequiresPermissions("trade:checkout:quote")
    public R<CheckoutQuoteRes> quote(
            @PathVariable Long memberId, @Valid @RequestBody CheckoutQuoteReq req) {
        return R.ok(
                CheckoutResConverter.checkoutQuote(
                        quote.execute(CheckoutReqConverter.quote(memberId, req))));
    }

    @Operation(summary = "幂等提交结算，返回RESERVING，Outbox确认库存后方可支付")
    @PostMapping("/members/{memberId}/checkout")
    @RequiresPermissions("trade:checkout:create")
    public R<CheckoutReceiptRes> checkout(
            @PathVariable Long memberId, @Valid @RequestBody CheckoutSubmitReq req) {
        return R.ok(
                CheckoutResConverter.checkoutReceipt(
                        checkout.execute(CheckoutReqConverter.checkout(memberId, req))));
    }

    @Operation(summary = "查询结算原始金额、优惠及当前订单状态")
    @GetMapping("/checkouts/{orderNo}")
    @RequiresPermissions("trade:checkout:query")
    public R<CheckoutReceiptRes> receipt(@PathVariable String orderNo) {
        var value = receipt.execute(orderNo);
        return value == null
                ? R.fail("结算记录不存在")
                : R.ok(CheckoutResConverter.checkoutReceipt(value));
    }
}

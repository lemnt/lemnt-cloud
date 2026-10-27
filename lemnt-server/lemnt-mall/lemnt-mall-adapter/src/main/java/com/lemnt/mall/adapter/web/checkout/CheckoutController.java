package com.lemnt.mall.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutReqConverter;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.web.checkout.req.CheckoutQuoteReq;
import com.lemnt.mall.adapter.web.checkout.req.CheckoutSubmitReq;
import com.lemnt.mall.adapter.web.checkout.res.CheckoutQuoteRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCheckoutCheckoutRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCheckoutReceiptRes;
import com.lemnt.mall.application.checkout.executor.CheckoutQuoteQueryExe;
import com.lemnt.mall.application.checkout.executor.CheckoutReceiptQueryExe;
import com.lemnt.mall.application.checkout.executor.CheckoutSubmitCmdExe;

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
 * 后台购物车结算。
 */
@Tag(name = "后台购物车结算")
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutQuoteQueryExe checkoutQuoteQueryExe;
    private final CheckoutSubmitCmdExe checkoutSubmitCmdExe;
    private final CheckoutReceiptQueryExe checkoutReceiptQueryExe;

    @Operation(summary = "服务端重新计价的结算预览，不锁券也不预占库存")
    @PostMapping("/members/{memberId}/checkout/quote")
    @RequiresPermissions("mall:checkout:quote")
    public R<CheckoutQuoteRes> quote(
            @PathVariable Long memberId, @Valid @RequestBody CheckoutQuoteReq req) {
        return R.ok(
                CheckoutResConverter.checkoutQuote(
                checkoutQuoteQueryExe.execute(CheckoutReqConverter.quote(memberId, req))));
    }

    @Operation(summary = "幂等提交结算，返回RESERVING，Outbox确认库存后方可支付")
    @PostMapping("/members/{memberId}/checkout")
    @RequiresPermissions("mall:checkout:create")
    public R<MallCheckoutCheckoutRes> checkout(
            @PathVariable Long memberId, @Valid @RequestBody CheckoutSubmitReq req) {
        return R.ok(
                CheckoutResConverter.checkoutReceiptToMallCheckoutCheckoutRes(
                checkoutSubmitCmdExe.execute(CheckoutReqConverter.checkout(memberId, req))));
    }

    @Operation(summary = "查询结算原始金额、优惠及当前订单状态")
    @GetMapping("/checkouts/{orderNo}")
    @RequiresPermissions("mall:checkout:query")
    public R<MallCheckoutReceiptRes> receipt(@PathVariable String orderNo) {
        var value = checkoutReceiptQueryExe.execute(orderNo);
        return value == null
                ? R.fail("结算记录不存在")
                : R.ok(CheckoutResConverter.checkoutReceiptToMallCheckoutReceiptRes(value));
    }
}

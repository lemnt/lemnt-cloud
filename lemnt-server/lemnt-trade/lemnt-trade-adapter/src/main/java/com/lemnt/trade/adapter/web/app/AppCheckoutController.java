package com.lemnt.trade.adapter.web.app;

import com.lemnt.common.core.domain.R;
import com.lemnt.trade.adapter.web.checkout.req.*;
import com.lemnt.trade.adapter.web.checkout.res.*;
import com.lemnt.trade.adapter.web.checkout.converter.*;
import com.lemnt.trade.application.checkout.executor.*;
import com.lemnt.trade.application.checkout.cmd.CartDeleteCmd;
import com.lemnt.trade.application.checkout.service.MemberSessionService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Tag(name = "小程序会员购物车与结算")
@RestController
@RequestMapping("/trade/app")
@RequiredArgsConstructor
public class AppCheckoutController {
    private final MemberSessionService sessions;
    private final CartListQueryExe list;
    private final CartSaveCmdExe save;
    private final CartDeleteCmdExe delete;
    private final CouponListQueryExe coupons;
    private final CheckoutQuoteQueryExe quote;
    private final CheckoutSubmitCmdExe checkout;
    private final CheckoutReceiptQueryExe receipt;

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    public R<Void> unauthorized() {
        return R.fail("会员登录已失效");
    }

    @GetMapping("/cart")
    @Operation(summary = "查询当前会员购物车")
    public R<List<CartItemRes>> cart(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long memberId = sessions.requireMember(token);
        return R.ok(list.execute(memberId).stream().map(CheckoutResConverter::cartItem).toList());
    }

    @PutMapping("/cart/{productId}")
    @Operation(summary = "更新当前会员购物车，不接受客户端价格或会员身份")
    public R<CartItemRes> save(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long productId,
            @Valid @RequestBody CartSaveReq req) {
        Long memberId = sessions.requireMember(token);
        return R.ok(
                CheckoutResConverter.cartItem(
                        save.execute(CheckoutReqConverter.cart(memberId, productId, req))));
    }

    @DeleteMapping("/cart/{productId}")
    @Operation(summary = "删除当前会员购物车条目")
    public R<Boolean> delete(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long productId) {
        Long memberId = sessions.requireMember(token);
        return R.ok(delete.execute(new CartDeleteCmd(memberId, productId)));
    }

    @GetMapping("/coupons")
    @Operation(summary = "当前会员持有的优惠券")
    public R<List<CouponRes>> coupons(
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long memberId = sessions.requireMember(token);
        return R.ok(coupons.execute(memberId).stream().map(CheckoutResConverter::coupon).toList());
    }

    @PostMapping("/checkout/quote")
    @Operation(summary = "当前会员结算预览，服务端计价")
    public R<CheckoutQuoteRes> quote(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody CheckoutQuoteReq req) {
        Long memberId = sessions.requireMember(token);
        return R.ok(
                CheckoutResConverter.checkoutQuote(
                        quote.execute(CheckoutReqConverter.quote(memberId, req))));
    }

    @PostMapping("/checkout")
    @Operation(summary = "当前会员幂等下单，不接受客户端金额")
    public R<CheckoutReceiptRes> checkout(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody CheckoutSubmitReq req) {
        Long memberId = sessions.requireMember(token);
        return R.ok(
                CheckoutResConverter.checkoutReceipt(
                        checkout.execute(CheckoutReqConverter.checkout(memberId, req))));
    }

    @GetMapping("/orders/{orderNo}")
    @Operation(summary = "查询本人订单结算回执，他人订单与不存在统一返回不存在")
    public R<CheckoutReceiptRes> order(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String orderNo) {
        Long memberId = sessions.requireMember(token);
        var value = receipt.execute(memberId, orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(CheckoutResConverter.checkoutReceipt(value));
    }
}

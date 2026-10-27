package com.lemnt.mall.adapter.app.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutReqConverter;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.checkout.converter.SettlementReqConverter;
import com.lemnt.mall.adapter.app.checkout.converter.SettlementResConverter;
import com.lemnt.mall.adapter.app.checkout.req.CheckoutQuoteReq;
import com.lemnt.mall.adapter.app.checkout.req.MallAppCheckoutAbandonReq;
import com.lemnt.mall.adapter.app.checkout.req.MallAppCheckoutCheckoutReq;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCheckoutCheckoutRes;
import com.lemnt.mall.adapter.app.checkout.res.SettlementAbandonRes;
import com.lemnt.mall.adapter.app.checkout.res.SettlementRes;
import com.lemnt.mall.application.checkout.executor.SettlementAbandonCmdExe;
import com.lemnt.mall.application.checkout.executor.SettlementCreateCmdExe;
import com.lemnt.mall.application.checkout.executor.SettlementSubmitCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序结算接口。 */
@Tag(name = "小程序结算")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCheckoutController {
    private final SettlementCreateCmdExe settlementCreateCmdExe;
    private final SettlementSubmitCmdExe settlementSubmitCmdExe;
    private final SettlementAbandonCmdExe settlementAbandonCmdExe;

    @PostMapping("/checkout/quote")
    @Operation(summary = "当前会员结算预览，服务端计价")
    public R<SettlementRes> quote(
            @Valid @RequestBody CheckoutQuoteReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                SettlementResConverter.convert(
                settlementCreateCmdExe.execute(CheckoutReqConverter.quote(memberId, req))));
    }

    @PostMapping("/checkout")
    @Operation(summary = "当前会员幂等下单，不接受客户端金额")
    public R<MallAppCheckoutCheckoutRes> checkout(
            @Valid @RequestBody MallAppCheckoutCheckoutReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                CheckoutResConverter.checkoutReceiptToMallAppCheckoutCheckoutRes(
                settlementSubmitCmdExe.execute(SettlementReqConverter.submit(memberId, req))));
    }

    @PostMapping("/checkout/abandon")
    @Operation(summary = "终止尚未完成的提单；已生成订单则返回原订单，不执行关单")
    public R<SettlementAbandonRes> abandon(
            @Valid @RequestBody MallAppCheckoutAbandonReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                SettlementResConverter.abandon(
                settlementAbandonCmdExe.execute(SettlementReqConverter.abandon(memberId, req))));
    }
}

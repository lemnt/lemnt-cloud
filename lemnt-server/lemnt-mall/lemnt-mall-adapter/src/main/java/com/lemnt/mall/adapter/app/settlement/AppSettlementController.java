package com.lemnt.mall.adapter.app.settlement;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementQuoteReqConverter;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementQuoteResConverter;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementReqConverter;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementResConverter;
import com.lemnt.mall.adapter.app.settlement.req.SettlementAbandonReq;
import com.lemnt.mall.adapter.app.settlement.req.SettlementQuoteReq;
import com.lemnt.mall.adapter.app.settlement.req.SettlementSubmitReq;
import com.lemnt.mall.adapter.app.settlement.res.SettlementAbandonRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementSubmitRes;
import com.lemnt.mall.application.settlement.executor.SettlementAbandonCmdExe;
import com.lemnt.mall.application.settlement.executor.SettlementCreateCmdExe;
import com.lemnt.mall.application.settlement.executor.SettlementSubmitCmdExe;

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
public class AppSettlementController {
    private final SettlementCreateCmdExe settlementCreateCmdExe;
    private final SettlementSubmitCmdExe settlementSubmitCmdExe;
    private final SettlementAbandonCmdExe settlementAbandonCmdExe;

    @PostMapping("/checkout/quote")
    @Operation(summary = "当前会员结算预览，服务端计价")
    public R<SettlementRes> quote(
            @Valid @RequestBody SettlementQuoteReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                SettlementResConverter.convert(
                settlementCreateCmdExe.execute(SettlementQuoteReqConverter.quote(memberId, req))));
    }

    @PostMapping("/checkout")
    @Operation(summary = "当前会员幂等下单，不接受客户端金额")
    public R<SettlementSubmitRes> checkout(
            @Valid @RequestBody SettlementSubmitReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                SettlementQuoteResConverter.submit(
                settlementSubmitCmdExe.execute(SettlementReqConverter.submit(memberId, req))));
    }

    @PostMapping("/checkout/abandon")
    @Operation(summary = "终止尚未完成的提单；已生成订单则返回原订单，不执行关单")
    public R<SettlementAbandonRes> abandon(
            @Valid @RequestBody SettlementAbandonReq req) {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(
                SettlementResConverter.abandon(
                settlementAbandonCmdExe.execute(SettlementReqConverter.abandon(memberId, req))));
    }
}

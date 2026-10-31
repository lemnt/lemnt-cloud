package com.lemnt.mall.adapter.app.settlement;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementQuoteResConverter;
import com.lemnt.mall.adapter.app.settlement.converter.SettlementReqConverter;
import com.lemnt.mall.adapter.app.settlement.res.MallAppOrderOrderRes;
import com.lemnt.mall.adapter.app.settlement.res.MallAppOrderRecoverRes;
import com.lemnt.mall.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.mall.application.settlement.executor.SettlementRequestDetailQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 小程序订单查询接口。 */
@Tag(name = "小程序订单查询")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppOrderController {
    private final SettlementReceiptQueryExe checkoutReceiptQueryExe;
    private final SettlementRequestDetailQueryExe settlementRequestDetailQueryExe;

    @GetMapping("/orders/{orderNo}")
    @Operation(summary = "查询本人订单结算回执，他人订单与不存在统一返回不存在")
    public R<MallAppOrderOrderRes> order(
            @PathVariable String orderNo) {
        Long memberId = SecurityUtils.getUserId();
        var value = checkoutReceiptQueryExe.execute(memberId, orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(SettlementQuoteResConverter.checkoutReceiptToMallAppOrderOrderRes(value));
    }

    @GetMapping("/checkout/requests/{requestNo}")
    @Operation(summary = "按本人提单请求编号找回订单，不依赖结算缓存；查无结果不代表请求已取消")
    public R<MallAppOrderRecoverRes> recover(
            @PathVariable String requestNo) {
        Long memberId = SecurityUtils.getUserId();
        var value = settlementRequestDetailQueryExe.execute(SettlementReqConverter.request(memberId, requestNo));
        return value == null
                ? R.fail("暂未查到订单，请保留原请求重试或继续核验")
                : R.ok(SettlementQuoteResConverter.checkoutReceiptToMallAppOrderRecoverRes(value));
    }
}

package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentReqConverter;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentResConverter;
import com.lemnt.trade.adapter.web.fulfillment.req.RefundPageReq;
import com.lemnt.trade.adapter.web.fulfillment.req.RefundRejectReq;
import com.lemnt.trade.adapter.web.fulfillment.res.RefundRequestRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeRefundRequestDetailRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeRefundRequestRejectRes;
import com.lemnt.trade.application.fulfillment.executor.RefundDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.RefundPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.RefundRejectCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 后台退款申请。
 */
@Tag(name = "后台退款申请")
@RestController
@RequestMapping("/trade/admin/refund-requests")
@RequiredArgsConstructor
public class RefundRequestController {
    private final RefundPageQueryExe refundPageQueryExe;
    private final RefundDetailQueryExe refundDetailQueryExe;
    private final RefundRejectCmdExe refundRejectCmdExe;

    @GetMapping
    @Operation(summary = "分页查询退款申请，申请不代表已退款")
    @RequiresPermissions("trade:refund:list")
    public R<RefundRequestRes> page(@Valid @ModelAttribute RefundPageReq req) {
        var page = refundPageQueryExe.execute(FulfillmentReqConverter.refunds(req));
        return R.page(page.records().stream().map(FulfillmentResConverter::refund).toList(), page.total());
    }

    @GetMapping("/{refundNo}")
    @Operation(summary = "查询退款申请详情")
    @RequiresPermissions("trade:refund:query")
    public R<TradeRefundRequestDetailRes> detail(@PathVariable String refundNo) {
        var value = refundDetailQueryExe.execute(refundNo);
        return value == null ? R.fail("退款申请不存在") : R.ok(FulfillmentResConverter.refundToTradeRefundRequestDetailRes(value));
    }

    @PostMapping("/{refundNo}/reject")
    @Operation(summary = "驳回退款申请并允许订单继续履约")
    @RequiresPermissions("trade:refund:reject")
    public R<TradeRefundRequestRejectRes> reject(
            @PathVariable String refundNo, @Valid @RequestBody RefundRejectReq req) {
        return R.ok(
                FulfillmentResConverter.refundToTradeRefundRequestRejectRes(
                refundRejectCmdExe.execute(FulfillmentReqConverter.reject(refundNo, req))));
    }
}

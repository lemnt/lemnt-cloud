package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.fulfillment.req.*;
import com.lemnt.trade.adapter.web.fulfillment.res.*;
import com.lemnt.trade.adapter.web.fulfillment.converter.*;
import com.lemnt.trade.application.fulfillment.executor.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "后台退款申请")
@RestController
@RequestMapping("/trade/admin/refund-requests")
@RequiredArgsConstructor
public class AdminRefundRequestController {
    private final RefundPageQueryExe page;
    private final RefundDetailQueryExe detail;
    private final RefundRejectCmdExe reject;

    @GetMapping
    @Operation(summary = "分页查询退款申请，申请不代表已退款")
    @RequiresPermissions("trade:refund:list")
    public R<RefundPageRes> page(@Valid @ModelAttribute RefundPageReq req) {
        return R.ok(
                FulfillmentResConverter.refunds(
                        page.execute(FulfillmentReqConverter.refunds(req))));
    }

    @GetMapping("/{refundNo}")
    @Operation(summary = "查询退款申请详情")
    @RequiresPermissions("trade:refund:query")
    public R<RefundRequestRes> detail(@PathVariable String refundNo) {
        var value = detail.execute(refundNo);
        return value == null ? R.fail("退款申请不存在") : R.ok(FulfillmentResConverter.refund(value));
    }

    @PostMapping("/{refundNo}/reject")
    @Operation(summary = "驳回退款申请并允许订单继续履约")
    @RequiresPermissions("trade:refund:reject")
    public R<RefundRequestRes> reject(
            @PathVariable String refundNo, @Valid @RequestBody RefundRejectReq req) {
        return R.ok(
                FulfillmentResConverter.refund(
                        reject.execute(FulfillmentReqConverter.reject(refundNo, req))));
    }
}

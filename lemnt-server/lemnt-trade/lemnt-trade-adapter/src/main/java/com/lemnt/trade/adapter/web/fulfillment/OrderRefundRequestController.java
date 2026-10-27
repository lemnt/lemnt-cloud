package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentReqConverter;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentResConverter;
import com.lemnt.trade.adapter.web.fulfillment.req.RefundRequestReq;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeOrderRefundRequestRefundRes;
import com.lemnt.trade.application.fulfillment.executor.RefundRequestCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 订单退款申请接口。 */
@Tag(name = "订单退款申请")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class OrderRefundRequestController {
    private final RefundRequestCmdExe refundRequestCmdExe;

    @PostMapping("/{orderNo}/refund-requests")
    @Operation(summary = "登记发货前整单退款申请，不执行渠道退款，不标记已退款")
    @RequiresPermissions("trade:refund:request")
    public R<TradeOrderRefundRequestRefundRes> refund(
            @PathVariable String orderNo, @Valid @RequestBody RefundRequestReq req) {
        return R.ok(
                FulfillmentResConverter.refundToTradeOrderRefundRequestRefundRes(
                refundRequestCmdExe.execute(FulfillmentReqConverter.refund(orderNo, req))));
    }
}

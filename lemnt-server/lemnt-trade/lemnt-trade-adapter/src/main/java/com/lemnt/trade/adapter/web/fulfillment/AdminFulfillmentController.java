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

@Tag(name = "后台订单履约")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class AdminFulfillmentController {
    private final OrderPageQueryExe page;
    private final OrderDetailQueryExe detail;
    private final ShipmentCreateCmdExe shipment;
    private final OrderReceiveCmdExe receive;
    private final RefundRequestCmdExe refund;

    @GetMapping
    @Operation(summary = "分页查询订单，不含收件人信息")
    @RequiresPermissions("trade:fulfillment:list")
    public R<OrderPageRes> page(@Valid @ModelAttribute OrderPageReq req) {
        return R.ok(
                FulfillmentResConverter.orders(page.execute(FulfillmentReqConverter.orders(req))));
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情，含收件人信息")
    @RequiresPermissions("trade:fulfillment:query")
    public R<OrderDetailRes> detail(@PathVariable String orderNo) {
        var value = detail.execute(orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(FulfillmentResConverter.detail(value));
    }

    @PostMapping("/{orderNo}/shipment")
    @Operation(summary = "已支付且库存确认完成后登记发货，幂等且物流不可改写")
    @RequiresPermissions("trade:fulfillment:ship")
    public R<ShipmentRes> ship(
            @PathVariable String orderNo, @Valid @RequestBody ShipmentCreateReq req) {
        return R.ok(
                FulfillmentResConverter.shipment(
                        shipment.execute(FulfillmentReqConverter.shipment(orderNo, req))));
    }

    @PostMapping("/{orderNo}/receive")
    @Operation(summary = "后台登记收货，重复请求返回原收货时间")
    @RequiresPermissions("trade:fulfillment:receive")
    public R<ShipmentRes> receive(@PathVariable String orderNo) {
        return R.ok(FulfillmentResConverter.shipment(receive.execute(orderNo)));
    }

    @PostMapping("/{orderNo}/refund-requests")
    @Operation(summary = "登记发货前整单退款申请，不执行渠道退款，不标记已退款")
    @RequiresPermissions("trade:refund:request")
    public R<RefundRequestRes> refund(
            @PathVariable String orderNo, @Valid @RequestBody RefundRequestReq req) {
        return R.ok(
                FulfillmentResConverter.refund(
                        refund.execute(FulfillmentReqConverter.refund(orderNo, req))));
    }
}

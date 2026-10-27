package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentReqConverter;
import com.lemnt.trade.adapter.web.fulfillment.converter.FulfillmentResConverter;
import com.lemnt.trade.adapter.web.fulfillment.req.OrderPageReq;
import com.lemnt.trade.adapter.web.fulfillment.res.OrderDetailRes;
import com.lemnt.trade.adapter.web.fulfillment.res.OrderSummaryRes;
import com.lemnt.trade.application.fulfillment.executor.OrderDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.OrderPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 订单履约查询接口。 */
@Tag(name = "订单履约查询")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class FulfillmentOrderController {
    private final OrderPageQueryExe orderPageQueryExe;
    private final OrderDetailQueryExe orderDetailQueryExe;

    @GetMapping
    @Operation(summary = "分页查询订单，不含收件人信息")
    @RequiresPermissions("trade:fulfillment:list")
    public R<OrderSummaryRes> page(@Valid @ModelAttribute OrderPageReq req) {
        var page = orderPageQueryExe.execute(FulfillmentReqConverter.orders(req));
        return R.page(page.records().stream().map(FulfillmentResConverter::summary).toList(), page.total());
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情，含收件人信息")
    @RequiresPermissions("trade:fulfillment:query")
    public R<OrderDetailRes> detail(@PathVariable String orderNo) {
        var value = orderDetailQueryExe.execute(orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(FulfillmentResConverter.detail(value));
    }
}

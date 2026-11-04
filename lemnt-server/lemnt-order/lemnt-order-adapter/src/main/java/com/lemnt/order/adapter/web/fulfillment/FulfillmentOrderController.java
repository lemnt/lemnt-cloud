package com.lemnt.order.adapter.web.fulfillment;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.order.adapter.web.fulfillment.converter.FulfillmentReqConverter;
import com.lemnt.order.adapter.web.fulfillment.converter.FulfillmentResConverter;
import com.lemnt.order.adapter.web.fulfillment.req.OrderPageReq;
import com.lemnt.order.adapter.web.fulfillment.res.OrderDetailRes;
import com.lemnt.order.adapter.web.fulfillment.res.OrderSummaryRes;
import com.lemnt.order.adapter.web.promotion.converter.OrderPromotionResConverter;
import com.lemnt.order.adapter.web.promotion.res.OrderPromotionDetailRes;
import com.lemnt.order.application.fulfillment.executor.OrderDetailQueryExe;
import com.lemnt.order.application.fulfillment.executor.OrderPageQueryExe;
import com.lemnt.order.application.promotion.executor.OrderPromotionDetailQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 订单履约查询接口。
 */
@Tag(name = "订单履约查询")
@RestController
@RequestMapping("/trade/admin/orders")
public class FulfillmentOrderController {
    private final OrderPageQueryExe orderPageQueryExe;
    private final OrderDetailQueryExe orderDetailQueryExe;

    @GetMapping
    @Operation(summary = "分页查询订单，不含收件人信息")
    @RequiresPermissions("trade:fulfillment:list")
    public R<OrderSummaryRes> page(@Valid @ModelAttribute OrderPageReq req) {
        var page = orderPageQueryExe.execute(FulfillmentReqConverter.orders(req));
        return R.page(
            page.records().stream().map(FulfillmentResConverter::summary).toList(),
            page.total());
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情，含收件人信息")
    @RequiresPermissions("trade:fulfillment:query")
    public R<OrderDetailRes> detail(@PathVariable String orderNo) {
        var value = orderDetailQueryExe.execute(orderNo);
        return value == null ? R.fail("订单不存在") : R.ok(FulfillmentResConverter.detail(value));
    }

    private final OrderPromotionDetailQueryExe promotionQuery;

    /**
     * 查询冻结优惠及权益，不回查当前促销规则。
     */
    @GetMapping("/{orderNo}/promotions")
    @Operation(summary = "订单优惠快照")
    @RequiresPermissions("trade:fulfillment:query")
    public R<OrderPromotionDetailRes> promotions(@PathVariable String orderNo) {
        return R.ok(OrderPromotionResConverter.toRes(promotionQuery.execute(orderNo)));
    }

    /**
     * 生产使用完整查询协作者，存量测试构造仅服务原有路由。
     */
    @Autowired
    public FulfillmentOrderController(
        OrderPageQueryExe orderPageQueryExe,
        OrderDetailQueryExe orderDetailQueryExe,
        OrderPromotionDetailQueryExe promotionQuery) {
        this.orderPageQueryExe = orderPageQueryExe;
        this.orderDetailQueryExe = orderDetailQueryExe;
        this.promotionQuery = promotionQuery;
    }

    public FulfillmentOrderController(
        OrderPageQueryExe orderPageQueryExe, OrderDetailQueryExe orderDetailQueryExe) {
        this(orderPageQueryExe, orderDetailQueryExe, null);
    }
}

package com.lemnt.order.adapter.web.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.web.order.converter.OrderManagementReqConverter;
import com.lemnt.order.adapter.web.order.converter.OrderManagementResConverter;
import com.lemnt.order.adapter.web.order.req.OrderAdjustAddressReq;
import com.lemnt.order.adapter.web.order.req.OrderAdjustPriceReq;
import com.lemnt.order.adapter.web.order.req.OrderRemarkUpdateReq;
import com.lemnt.order.adapter.web.order.req.OrderStatisticsReq;
import com.lemnt.order.adapter.web.order.res.OrderRemarkRes;
import com.lemnt.order.adapter.web.order.res.OrderStatisticsRes;
import com.lemnt.order.application.order.cmd.OrderAdjustAddressCmd;
import com.lemnt.order.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.order.application.order.executor.OrderAdjustAddressCmdExe;
import com.lemnt.order.application.order.executor.OrderAdjustPriceCmdExe;
import com.lemnt.order.application.order.executor.OrderRemarkDetailQueryExe;
import com.lemnt.order.application.order.executor.OrderRemarkUpdateCmdExe;
import com.lemnt.order.application.order.executor.OrderStatisticsQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 订单后台统计与备注管理。
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderStatisticsQueryExe statistics;
    private final OrderRemarkDetailQueryExe remark;
    private final OrderRemarkUpdateCmdExe updateRemark;
    private final OrderAdjustPriceCmdExe adjustPrice;
    private final OrderAdjustAddressCmdExe adjustAddress;

    /**
     * 应付与退款分别展示，避免将待处理售后当作已退款。
     */
    @GetMapping("/summary")
    @Operation(summary = "订单管理统计")
    @RequiresPermissions("trade:order:summary")
    public R<OrderStatisticsRes> statistics(@Valid @ModelAttribute OrderStatisticsReq req) {
        return R.ok(OrderManagementResConverter.statistics(statistics.execute(req.orderNo())));
    }

    /**
     * 查询后台备注。
     */
    @GetMapping("/{orderNo}/remark")
    @Operation(summary = "订单备注查询")
    @RequiresPermissions("trade:order:query")
    public R<OrderRemarkRes> remark(@PathVariable String orderNo) {
        var result = remark.execute(orderNo);
        return result == null ? R.fail("订单不存在") : R.ok(OrderManagementResConverter.remark(result));
    }

    /**
     * 只修改备注，不改写报价或支付金额。
     */
    @PutMapping("/{orderNo}/remark")
    @Operation(summary = "修改订单备注")
    @RequiresPermissions("trade:order:remark")
    public R<Boolean> remark(
        @PathVariable String orderNo, @Valid @RequestBody OrderRemarkUpdateReq req) {
        return R.ok(updateRemark.execute(OrderManagementReqConverter.remark(orderNo, req)));
    }

    /**
     * 待支付订单调价，金额重分摊到商品行并写操作日志。
     */
    @PostMapping("/{orderNo}/price-adjustment")
    @Operation(summary = "订单调价")
    @RequiresPermissions("trade:order:update")
    public R<Boolean> adjustPrice(
        @PathVariable String orderNo, @Valid @RequestBody OrderAdjustPriceReq req) {
        adjustPrice.execute(
            new OrderAdjustPriceCmd(orderNo, req.totalAmount(), SecurityUtils.getUserId()));
        return R.ok(true);
    }

    /**
     * 未发货快递单改址，写入收件快照与操作日志。
     */
    @PutMapping("/{orderNo}/address")
    @Operation(summary = "订单改址")
    @RequiresPermissions("trade:order:update")
    public R<Boolean> adjustAddress(
        @PathVariable String orderNo, @Valid @RequestBody OrderAdjustAddressReq req) {
        adjustAddress.execute(
            new OrderAdjustAddressCmd(
                orderNo,
                req.receiverName(),
                req.receiverMobile(),
                req.receiverAreaId(),
                req.receiverDetailAddress(),
                SecurityUtils.getUserId()));
        return R.ok(true);
    }
}

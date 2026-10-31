package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.order.converter.OrderManagementReqConverter;
import com.lemnt.trade.adapter.web.order.converter.OrderManagementResConverter;
import com.lemnt.trade.adapter.web.order.req.OrderRemarkUpdateReq;
import com.lemnt.trade.adapter.web.order.req.OrderStatisticsReq;
import com.lemnt.trade.adapter.web.order.res.OrderRemarkRes;
import com.lemnt.trade.adapter.web.order.res.OrderStatisticsRes;
import com.lemnt.trade.application.order.executor.OrderRemarkDetailQueryExe;
import com.lemnt.trade.application.order.executor.OrderRemarkUpdateCmdExe;
import com.lemnt.trade.application.order.executor.OrderStatisticsQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 订单后台统计与备注管理。 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/trade/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderStatisticsQueryExe statistics;
    private final OrderRemarkDetailQueryExe remark;
    private final OrderRemarkUpdateCmdExe updateRemark;

    /** 应付与退款分别展示，避免将待处理售后当作已退款。 */
    @GetMapping("/summary")
    @Operation(summary = "订单管理统计")
    @RequiresPermissions("trade:order:summary")
    public R<OrderStatisticsRes> statistics(@Valid @ModelAttribute OrderStatisticsReq req) {
        return R.ok(OrderManagementResConverter.statistics(statistics.execute(req.orderNo())));
    }

    /** 查询后台备注。 */
    @GetMapping("/{orderNo}/remark")
    @Operation(summary = "订单备注查询")
    @RequiresPermissions("trade:order:query")
    public R<OrderRemarkRes> remark(@PathVariable String orderNo) {
        var result = remark.execute(orderNo);
        return result == null ? R.fail("订单不存在") : R.ok(OrderManagementResConverter.remark(result));
    }

    /** 只修改备注，不改写报价或支付金额。 */
    @PutMapping("/{orderNo}/remark")
    @Operation(summary = "修改订单备注")
    @RequiresPermissions("trade:order:remark")
    public R<Boolean> remark(@PathVariable String orderNo, @Valid @RequestBody OrderRemarkUpdateReq req) {
        return R.ok(updateRemark.execute(OrderManagementReqConverter.remark(orderNo, req)));
    }
}

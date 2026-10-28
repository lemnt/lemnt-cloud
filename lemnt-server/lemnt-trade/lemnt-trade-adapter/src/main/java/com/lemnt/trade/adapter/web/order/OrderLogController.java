package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.adapter.web.order.converter.OrderLogResConverter;
import com.lemnt.trade.adapter.web.order.res.OrderLogRes;
import com.lemnt.trade.application.order.executor.OrderLogListQueryExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** 后台订单日志查询。 */
@Tag(name = "订单操作日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/admin/orders")
public class OrderLogController {
    private final OrderLogListQueryExe logs;
    /** 仅持有订单查询权限的后台用户可读。 */
    @GetMapping("/{orderNo}/logs")
    @Operation(summary = "查询订单操作日志")
    @RequiresPermissions("trade:fulfillment:query")
    public R<List<OrderLogRes>> logs(@PathVariable String orderNo) {
        return R.ok(logs.execute(orderNo).stream().map(OrderLogResConverter::response).toList());
    }
}

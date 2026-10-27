package com.lemnt.trade.adapter.web.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.application.order.executor.TradeOrderDetailQueryExe;
import com.lemnt.trade.domain.order.model.TradeOrder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易订单接口。
 */
@RestController
@RequestMapping("/trade/orders")
@RequiredArgsConstructor
public class TradeOrderController {
    private final TradeOrderDetailQueryExe detailExe;

    /**
     * 明细响应。
     */
    public record ItemRes(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount
    ) {}

    /**
     * 订单响应。
     */
    public record OrderRes(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<ItemRes> items
    ) {
        static OrderRes from(TradeOrder o) {
            return new OrderRes(
                    o.id(),
                    o.orderNo(),
                    o.memberId(),
                    o.totalAmount(),
                    o.status(),
                    o.items().stream()
                    .map(
                    i ->
                    new ItemRes(
                    i.productId(),
                    i.productCode(),
                    i.productName(),
                    i.unitPrice(),
                    i.quantity(),
                    i.lineAmount()))
                    .toList());
        }
    }

    /**
     * 详情。
     */
    @GetMapping("/{orderNo}")
    @RequiresPermissions("trade:order:query")
    public R<OrderRes> detail(@PathVariable String orderNo) {
        TradeOrder o = detailExe.execute(orderNo);
        return o == null ? R.fail("订单不存在") : R.ok(OrderRes.from(o));
    }
}

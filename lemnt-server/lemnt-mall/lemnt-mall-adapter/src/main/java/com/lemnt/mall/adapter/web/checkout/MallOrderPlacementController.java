package com.lemnt.mall.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd;
import com.lemnt.mall.application.checkout.executor.CheckoutDirectCmdExe;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 交易订单接口。
 */
@RestController
@RequestMapping("/mall/orders")
@RequiredArgsConstructor
public class MallOrderPlacementController {
    private final CheckoutDirectCmdExe createExe;

    /**
     * 明细请求。
     */
    public record ItemReq(
            @NotNull
            @Positive
            Long productId,
            @NotNull
            @Positive
            Integer quantity
    ) {}

    /**
     * 创建请求。
     */
    public record CreateReq(
            @NotNull
            @Positive
            Long memberId,
            @NotEmpty
            List<@Valid
            ItemReq> items,
            @Size(max = 64)
            String requestNo,
            @Positive
            Long couponId
    ) {}

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
        static OrderRes from(OrderSnapshot o) {
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
     * 创建。
     */
    @PostMapping
    @RequiresPermissions("mall:order:create")
    public R<OrderRes> create(@Valid @RequestBody CreateReq req) {
        var lines =
                req.items().stream()
                .map(i -> new CheckoutDirectCmd.OrderLine(i.productId(), i.quantity()))
                .toList();
        return R.ok(
                OrderRes.from(
                createExe.execute(
                new CheckoutDirectCmd(
                req.memberId(), lines, req.requestNo(), req.couponId()))));
    }

}

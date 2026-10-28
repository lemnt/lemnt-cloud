package com.lemnt.trade.adapter.app.order;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.trade.application.fulfillment.executor.OrderReceiveCmdExe;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 会员本人取消及收货，身份仅取网关已认证上下文。 */
@Tag(name = "会员订单操作")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trade/app/orders")
public class AppOrderController {
    private final TradeOrderCloseCmdExe close;
    private final OrderReceiveCmdExe receive;
    /** 取消本人未付款订单，库存与券统一异步回滚。 */
    @PostMapping("/{orderNo}/cancel")
    @Operation(summary = "取消本人未支付订单")
    public R<Boolean> cancel(@PathVariable String orderNo) {
        if (!close.executeForMember(SecurityUtils.getUserId(), orderNo)) {
            throw new ServiceException("订单不允许取消", 400);
        }
        return R.ok(true);
    }
    /** 核验订单归属后确认本人收货。 */
    @PostMapping("/{orderNo}/receive")
    @Operation(summary = "本人确认收货")
    public R<Boolean> receive(@PathVariable String orderNo) {
        receive.executeForMember(SecurityUtils.getUserId(), orderNo);
        return R.ok(true);
    }
}

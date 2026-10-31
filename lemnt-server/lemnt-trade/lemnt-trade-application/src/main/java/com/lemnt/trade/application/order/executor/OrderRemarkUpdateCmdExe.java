package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.cmd.OrderRemarkUpdateCmd;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 后台订单备注修改。 */
@Component
@RequiredArgsConstructor
public class OrderRemarkUpdateCmdExe {
    private final TradeOrderGateway orders;

    /** 备注修改与订单状态串行，不参与金额或库存计算。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(OrderRemarkUpdateCmd cmd) {
        if (cmd == null || cmd.remark() == null || cmd.remark().length() > 500) {
            throw new IllegalArgumentException("备注参数不合法");
        }
        String no = OrderRules.text(cmd.orderNo(), 40);
        if (orders.lockHeader(no) == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!orders.updateRemark(no, cmd.remark())) {
            throw new IllegalStateException("订单备注修改失败");
        }
        return true;
    }
}

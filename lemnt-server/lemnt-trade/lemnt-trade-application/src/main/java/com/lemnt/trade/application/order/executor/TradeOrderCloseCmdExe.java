package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.domain.order.model.TradeOperator;


import com.lemnt.trade.application.order.service.OrderCloseService;
import com.lemnt.trade.application.order.service.OrderRemoteService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 关单本地事务提交后，异步发送库存与优惠券统一回滚补偿。 */
@Component
@RequiredArgsConstructor
public class TradeOrderCloseCmdExe {
    private final OrderCloseService orders;
    private final OrderRemoteService remotes;

    /** 关闭本地订单事务结束后再提交统一补偿消息。 */
    public boolean execute(String orderNo) {
        if (!orders.execute(orderNo)) {
            return false;
        }
        remotes.closed(orderNo);
        return true;
    }
    /** 本人取消未支付订单，事务提交后发送统一回滚补偿。 */
    public boolean executeForMember(Long memberId, String orderNo) {
        if (!orders.execute(orderNo, TradeOperator.member(memberId))) {
            return false;
        }
        remotes.closed(orderNo);
        return true;
    }
}

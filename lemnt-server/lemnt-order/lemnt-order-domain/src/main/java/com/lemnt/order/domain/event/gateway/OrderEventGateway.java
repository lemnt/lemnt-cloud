package com.lemnt.order.domain.event.gateway;

import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.order.model.TradeOrder;

/**
 * 业务状态与冻结订单事实必须同事务登记，不在这里发送网络消息。
 */
public interface OrderEventGateway {
    void paid(TradeOrder order);

    void closed(TradeOrder order);

    void shipped(TradeOrder order);

    void completed(TradeOrder order);

    void afterSale(AfterSale afterSale, TradeOrder order, boolean fullyRefunded);

    boolean replayAfterSale(String afterSaleNo);
}

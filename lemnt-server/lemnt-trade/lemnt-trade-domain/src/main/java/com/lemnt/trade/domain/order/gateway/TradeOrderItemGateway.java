package com.lemnt.trade.domain.order.gateway;

import com.lemnt.trade.domain.order.model.OrderRefundItem;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import java.util.List;

/** TradeOrderItem 表领域存储接口。 */
public interface TradeOrderItemGateway {
    /** 按持久化主键顺序读取商品行，供退款金额分摊。 */
    List<OrderRefundItem> refundItems(Long orderId);

    /** 保存订单的明细集合。 */
    void saveItems(Long orderId, List<TradeOrderItem> items);

    /** 只修改真实所属订单项的评价状态。 */
    boolean markCommented(Long orderId, Long orderItemId);

    /** 读取订单明细。 */
    List<TradeOrderItem> items(Long orderId);

    /** 后台调价后按分摊结果更新单行实付。 */
    boolean updatePayAmount(Long orderId, Long orderItemId, java.math.BigDecimal payAmount);

    /** 持有订单锁且售后首次成功后增加已退数量，历史NULL从成功事实核对后初始化。 */
    boolean recordRefunded(Long orderId, Long orderItemId, int previous, int delta);
}

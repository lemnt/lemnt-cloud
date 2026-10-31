package com.lemnt.trade.domain.order.gateway;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.fulfillment.model.OrderPage;
import com.lemnt.trade.domain.order.model.OrderStatusCount;
import com.lemnt.trade.domain.order.model.OrderTotal;
import com.lemnt.trade.domain.order.model.TradeOrder;

import java.time.LocalDateTime;
import java.util.List;

/** TradeOrder 表领域存储接口。 */
public interface TradeOrderGateway {
    /** 按精确订单号或全部订单统计数量与应付金额。 */
    OrderTotal total(String orderNo);

    /** 读取后台备注，不修改财务或履约状态。 */
    String remark(String orderNo);

    /** 修改后台备注，支持空字符串清除。 */
    boolean updateRemark(String orderNo, String remark);

    /**
     * 查询订单。
     */
    OrderPage orders(FulfillmentFilter filter);

    /** 按状态与评价状态统计会员本人订单数量。 */
    List<OrderStatusCount> memberStatusCounts(Long memberId);

    /**
     * 数据库当前时间。
     */
    LocalDateTime databaseNow();

    /**
     * 状态变更状态。
     */
    boolean transitionStatus(String orderNo, String expectedStatus, String targetStatus);

    /** 同时确认收货状态和真实收货时间，调用方持有订单锁。 */
    boolean completeReceipt(String orderNo, LocalDateTime receivedAt);
    /** 有界扫描及持锁后重新核验到期条件。 */
    List<String> commentExpired(LocalDateTime cutoff, int limit);
    boolean commentExpired(String orderNo, LocalDateTime cutoff);

    /**
     * 查找库存预留中且按创建时间筛选之前。
     */
    List<TradeOrder> findReservingCreatedBefore(LocalDateTime deadline, int limit);

    /**
     * 查找待处理支付创建时间之前。
     */
    List<TradeOrder> findPendingPaymentCreatedBefore(LocalDateTime deadline, int limit);

    /** 调用方已持有订单锁，保存真实评价汇总状态。 */
    boolean updateCommentState(com.lemnt.trade.domain.order.model.OrderCommentState value);

    /** 保存订单主体，明细由应用层编排。 */
    TradeOrder saveHeader(TradeOrder order);

    /** 读取订单主体。 */
    TradeOrder findHeader(String orderNo);

    /** 锁定订单主体。 */
    TradeOrder lockHeader(String orderNo);
}

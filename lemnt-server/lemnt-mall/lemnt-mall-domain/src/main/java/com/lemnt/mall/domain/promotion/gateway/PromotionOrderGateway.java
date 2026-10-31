package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionOrder;

/** 活动订单映射存储契约。 */
public interface PromotionOrderGateway {
    PromotionOrder findByOrderNo(String orderNo);

    PromotionOrder findByRequest(Long memberId, String requestNo);

    /** 锁定映射行，串行执行生命周期补偿。 */
    PromotionOrder lockByOrderNo(String orderNo);

    PromotionOrder save(PromotionOrder value);

    boolean markReserved(String orderNo);

    boolean markPaid(String orderNo);

    boolean markClosed(String orderNo);

    boolean markRefunded(String orderNo);

    boolean markFailed(String orderNo, String reason);

    /** 交易未建单时允许同一请求编号重试。 */
    boolean reopenFailed(String orderNo);

    boolean bindRecord(String orderNo, Long recordId);

    /** 条件累加已退款数量，重复售后回调不会重复恢复库存。 */
    boolean addRefundedQuantity(String orderNo, Integer expected, Integer quantity);

    /** 记录退款单号；重复退款单号返回 false。 */
    boolean recordRefund(String orderNo, String refundNo, Integer quantity);
}

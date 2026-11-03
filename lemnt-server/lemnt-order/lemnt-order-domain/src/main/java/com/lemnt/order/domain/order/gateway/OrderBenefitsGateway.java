package com.lemnt.order.domain.order.gateway;

import com.lemnt.order.domain.order.model.OrderRefundBenefits;

import java.math.BigDecimal;
import java.util.Map;

/** 通过商城服务处理订单事件，不共享商城表或事务。 */
public interface OrderBenefitsGateway {
    /** 下单时同步核验本单优惠券的会员、订单绑定和锁定状态。 */
    void verify(String orderNo);

    /** 库存预占完成。 */
    void reserved(String orderNo);

    /** 已支付。 */
    void paid(String orderNo);

    /** 已关闭。 */
    void closed(String orderNo);

    /** 支付退款成功后按售后单回补库存及券权益。 */
    void refunded(
            String orderNo,
            String refundNo,
            Map<Long, Integer> quantities,
            BigDecimal refundAmount);

    /** 新版明确商品行及积分结果，不能退回旧版按当前金额计算。 */
    default void refunded(OrderRefundBenefits plan) {
        throw new UnsupportedOperationException("下游未升级冻结权益协议");
    }
}

package com.lemnt.mall.client;

import com.lemnt.mall.client.bo.RefundBenefitsBo;
import com.lemnt.mall.client.bo.RefundStockBo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 接收交易服务提交成功后的订单事件；重复通知必须幂等。
 */
public interface CheckoutLifecycleClient {
    /**
     * 下单时同步核验本单优惠券的会员、订单绑定和锁定状态。
     */
    void verify(String orderNo);

    /**
     * 库存预占完成后消费购物车的对应版本。
     */
    void reserved(String orderNo);

    /**
     * 支付成功后核销优惠券。
     */
    void paid(String orderNo);

    /**
     * 订单关闭后释放优惠券。
     */
    void closed(String orderNo);

    /**
     * 商品售后成功后，统一回补库存并处理整单退款的券权益。
     */
    void refunded(
            String orderNo, String refundNo, List<RefundStockBo> items, BigDecimal refundAmount);

    /** 新版商品行及明确权益方案，未升级消费者必须失败并保留重试。 */
    default void refunded(RefundBenefitsBo plan) {
        throw new UnsupportedOperationException("未升级冻结权益协议");
    }
}

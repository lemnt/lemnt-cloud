package com.lemnt.mall.client;

/** 接收交易服务提交成功后的订单事件；重复通知必须幂等。 */
public interface CheckoutLifecycleClient {
    /** 库存预占完成后消费购物车的对应版本。 */
    void reserved(String orderNo);

    /** 支付成功后核销优惠券。 */
    void paid(String orderNo);

    /** 订单关闭后释放优惠券。 */
    void closed(String orderNo);
}

package com.lemnt.order.client;

import com.lemnt.order.client.bo.OrderCommentCreateBo;
import com.lemnt.order.client.bo.OrderCreateBo;
import com.lemnt.order.client.dto.OrderDTO;

/** 商城提交已确认报价的订单，订单及支付数据由交易服务管理。 */
public interface TradeOrderClient {
    /** 按订单号幂等保存订单及明细。 */
    OrderDTO create(OrderCreateBo order);

    /** 在订单锁事务内编排真实购买评价。 */
    Long createComment(OrderCommentCreateBo value);

    /** 查询订单快照。 */
    OrderDTO getByOrderNo(String orderNo);

    /** 完整报价专用建单入口，旧消费者不能丢弃新增金额/分摊字段后继续建单。 */
    default OrderDTO createPriced(OrderCreateBo order) {
        throw new UnsupportedOperationException("交易服务未升级完整报价协议");
    }
}

package com.lemnt.trade.client;

import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;

/** 商城提交已确认报价的订单，订单及支付数据由交易服务管理。 */
public interface TradeOrderClient {
    /** 按订单号幂等保存订单及明细。 */
    OrderDTO create(OrderCreateBo order);

    /** 在订单锁事务内编排真实购买评价。 */
    Long createComment(com.lemnt.trade.client.bo.OrderCommentCreateBo value);

    /** 查询订单快照。 */
    OrderDTO getByOrderNo(String orderNo);
}

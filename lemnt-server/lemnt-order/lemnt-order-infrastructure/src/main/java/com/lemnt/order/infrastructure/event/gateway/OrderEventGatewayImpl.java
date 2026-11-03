package com.lemnt.order.infrastructure.event.gateway;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.*;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.*;
/** 所有金额、积分、数量来自原订单/售后；重投复用已保存信封，不重新计算。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation=Propagation.MANDATORY,rollbackFor=Exception.class)
public class OrderEventGatewayImpl implements OrderEventGateway {
    private final MessageOutboxGateway outbox;
    public void paid(TradeOrder o){outbox.enqueue(CommerceEventTopics.ORDER_PAID,o.orderNo(),o.orderNo(),JsonUtils.toJsonStr(new OrderPaidEvent(o.orderNo(),o.memberId(),o.totalAmount(),o.usePoint(),o.givePoint(),o.giveCouponTemplateCounts(),stock(o))));}
    public void closed(TradeOrder o){outbox.enqueue(CommerceEventTopics.ORDER_CLOSED,o.orderNo(),o.orderNo(),JsonUtils.toJsonStr(new OrderClosedEvent(o.orderNo(),o.memberId(),stock(o))));}
    public void shipped(TradeOrder o){outbox.enqueue(CommerceEventTopics.ORDER_SHIPPED,o.orderNo(),o.orderNo(),JsonUtils.toJsonStr(new OrderShippedEvent(o.orderNo(),o.memberId())));}
    public void completed(TradeOrder o){outbox.enqueue(CommerceEventTopics.ORDER_COMPLETED,o.orderNo(),o.orderNo(),JsonUtils.toJsonStr(new OrderCompletedEvent(o.orderNo(),o.memberId())));}
    public void afterSale(AfterSale af,TradeOrder o,boolean full){
        if(!Integer.valueOf(1).equals(af.refundRuleVersion()) || af.orderItemId()==null || af.skuId()==null || af.quantity()==null || af.returnUsePoint()==null || af.deductGivePoint()==null)throw new IllegalStateException("售后冻结方案不完整，不能按当前规则补历史");
        outbox.enqueue(CommerceEventTopics.AFTER_SALE_COMPLETED,af.refundNo(),af.orderNo(),JsonUtils.toJsonStr(new AfterSaleCompletedEvent(af.orderNo(),af.refundNo(),af.memberId(),af.orderItemId(),af.skuId(),af.quantity(),af.amount(),af.returnUsePoint(),af.deductGivePoint(),af.refundRuleVersion(),full)));
    }
    public boolean replayAfterSale(String no){return outbox.replay(CommerceEventTopics.AFTER_SALE_COMPLETED,no);}
    private List<OrderStockLineDTO> stock(TradeOrder o){
        var values=new LinkedHashMap<Long,Integer>();for(var item:o.items())values.merge(item.productId(),item.quantity(),Math::addExact);
        return values.entrySet().stream().map(v->new OrderStockLineDTO(v.getKey(),v.getValue())).toList();
    }
}

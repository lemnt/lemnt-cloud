package com.lemnt.trade.infrastructure.order.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.trade.domain.order.gateway.OrderLogGateway;
import com.lemnt.trade.domain.order.model.OrderLog;
import com.lemnt.trade.infrastructure.order.converter.OrderLogConverter;
import com.lemnt.trade.infrastructure.order.dataobject.OrderLogDO;
import com.lemnt.trade.infrastructure.order.mapper.OrderLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/** 订单日志单表持久化实现。 */
@Component
@RequiredArgsConstructor
public class OrderLogGatewayImpl implements OrderLogGateway {
    private final OrderLogMapper mapper;
    @Override
    public void append(OrderLog value) { mapper.insert(OrderLogConverter.toDO(value)); }
    @Override
    public List<OrderLog> byOrderId(Long orderId) {
        return mapper.selectList(Wrappers.<OrderLogDO>lambdaQuery().eq(OrderLogDO::getOrderId, orderId)
                .orderByAsc(OrderLogDO::getId)).stream().map(OrderLogConverter::toModel).toList();
    }
}

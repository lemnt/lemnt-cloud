package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.order.domain.brokerage.gateway.OrderCommissionLineGateway;
import com.lemnt.order.domain.brokerage.model.FixedCommission;
import com.lemnt.order.domain.brokerage.model.OrderCommissionLine;
import com.lemnt.order.infrastructure.brokerage.dataobject.OrderCommissionLineDO;
import com.lemnt.order.infrastructure.brokerage.mapper.OrderCommissionLineMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderCommissionLineGatewayImpl implements OrderCommissionLineGateway {
    private final OrderCommissionLineMapper mapper;

    @Override
    public void insert(OrderCommissionLine line) {
        var fixed = line.fixed();
        if (mapper.insert(
                        new OrderCommissionLineDO()
                                .setOrderItemId(line.orderItemId())
                                .setOrderId(line.orderId())
                                .setFirstFixed(fixed == null ? null : fixed.first())
                                .setSecondFixed(fixed == null ? null : fixed.second()))
                != 1) {
            throw new IllegalStateException("佣金配置快照写入失败");
        }
    }

    @Override
    public List<OrderCommissionLine> lines(Long orderId) {
        return mapper
                .selectList(
                        Wrappers.<OrderCommissionLineDO>lambdaQuery()
                                .eq(OrderCommissionLineDO::getOrderId, orderId)
                                .orderByAsc(OrderCommissionLineDO::getOrderItemId))
                .stream()
                .map(
                        row ->
                                new OrderCommissionLine(
                                        row.getOrderId(),
                                        row.getOrderItemId(),
                                        row.getFirstFixed() == null
                                                ? null
                                                : new FixedCommission(
                                                        row.getFirstFixed(), row.getSecondFixed()),
                                        row.getFirstEarned(),
                                        row.getSecondEarned()))
                .toList();
    }

    @Override
    public boolean recordEarned(Long orderItemId, int level, BigDecimal earned) {
        SFunction<OrderCommissionLineDO, BigDecimal> column =
                switch (level) {
                    case 1 -> OrderCommissionLineDO::getFirstEarned;
                    case 2 -> OrderCommissionLineDO::getSecondEarned;
                    default -> throw new IllegalArgumentException("佣金等级无效");
                };
        return mapper.update(
                        null,
                        Wrappers.<OrderCommissionLineDO>lambdaUpdate()
                                .eq(OrderCommissionLineDO::getOrderItemId, orderItemId)
                                .isNull(column)
                                .set(column, earned))
                == 1;
    }
}

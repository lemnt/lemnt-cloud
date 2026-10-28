package com.lemnt.trade.infrastructure.config.gateway;

import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.config.model.TradeConfig;
import com.lemnt.trade.infrastructure.config.converter.TradeConfigConverter;
import com.lemnt.trade.infrastructure.config.mapper.TradeConfigMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** trade_config 持久化实现。 */
@Component
@RequiredArgsConstructor
public class TradeConfigGatewayImpl implements TradeConfigGateway {
    private final TradeConfigMapper mapper;

    @Override
    public TradeConfig get() {
        return TradeConfigConverter.toEntity(mapper.selectById(1L));
    }

    @Override
    public void lock() {
        if (mapper.lockById(1L) == null) {
            throw new IllegalStateException("交易配置缺失，请先执行初始化脚本");
        }
    }

    @Override
    public void update(TradeConfig value) {
        if (mapper.updateById(TradeConfigConverter.toDO(value)) != 1) {
            throw new IllegalStateException("交易配置保存失败");
        }
    }

}

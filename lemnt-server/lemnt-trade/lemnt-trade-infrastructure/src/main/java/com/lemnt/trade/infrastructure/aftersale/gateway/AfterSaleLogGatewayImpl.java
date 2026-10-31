package com.lemnt.trade.infrastructure.aftersale.gateway;

import com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSaleLog;
import com.lemnt.trade.infrastructure.aftersale.converter.AfterSaleLogConverter;
import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleLogDO;
import com.lemnt.trade.infrastructure.aftersale.mapper.AfterSaleLogMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 售后日志单表持久化实现。 */
@Component
@RequiredArgsConstructor
public class AfterSaleLogGatewayImpl implements AfterSaleLogGateway {
    private final AfterSaleLogMapper mapper;
    @Override
    public void append(AfterSaleLog value) {
        mapper.insert(AfterSaleLogConverter.toDO(value));
    }
    @Override
    public List<AfterSaleLog> byAfterSaleId(Long afterSaleId) {
        return mapper.selectList(Wrappers.<AfterSaleLogDO>lambdaQuery().eq(AfterSaleLogDO::getAfterSaleId, afterSaleId)
                .orderByAsc(AfterSaleLogDO::getId)).stream().map(AfterSaleLogConverter::toModel).toList();
    }
}

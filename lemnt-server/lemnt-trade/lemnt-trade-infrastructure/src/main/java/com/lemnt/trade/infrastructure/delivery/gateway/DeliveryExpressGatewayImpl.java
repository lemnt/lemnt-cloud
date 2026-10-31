package com.lemnt.trade.infrastructure.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpress;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressFilter;
import com.lemnt.trade.infrastructure.delivery.converter.DeliveryExpressConverter;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressDO;
import com.lemnt.trade.infrastructure.delivery.mapper.DeliveryExpressMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** trade_delivery_express 持久化实现。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressGatewayImpl implements DeliveryExpressGateway {
    private final DeliveryExpressMapper mapper;

    @Override
    public DeliveryExpress find(Long id) {
        return DeliveryExpressConverter.toEntity(mapper.selectById(id));
    }

    @Override
    public DeliveryExpress byCode(String code) {
        return DeliveryExpressConverter.toEntity(mapper.selectOne(Wrappers.<DeliveryExpressDO>lambdaQuery()
                .eq(DeliveryExpressDO::getCode, code)));
    }

    @Override
    public List<DeliveryExpress> list(String name, Integer status) {
        return mapper.selectList(Wrappers.<DeliveryExpressDO>lambdaQuery()
                .like(name != null && !name.isBlank(), DeliveryExpressDO::getName, name)
                .eq(status != null, DeliveryExpressDO::getStatus, status)
                .orderByAsc(DeliveryExpressDO::getSort).orderByDesc(DeliveryExpressDO::getId))
                .stream().map(DeliveryExpressConverter::toEntity).toList();
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }

    @Override
    public DeliveryExpress insert(DeliveryExpress value) {
        var row = DeliveryExpressConverter.toDO(value);
        mapper.insert(row);
        return DeliveryExpressConverter.toEntity(row);
    }

    @Override
    public boolean update(DeliveryExpress value) {
        return mapper.updateById(DeliveryExpressConverter.toDO(value)) == 1;
    }

    @Override
    public PageResult<DeliveryExpress> page(DeliveryExpressFilter filter) {
        var page = mapper.selectPage(new Page<DeliveryExpressDO>(filter.pageNum(), filter.pageSize()),
                Wrappers.<DeliveryExpressDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(), DeliveryExpressDO::getName, filter.name())
                        .eq(filter.status() != null, DeliveryExpressDO::getStatus, filter.status())
                        .orderByAsc(DeliveryExpressDO::getSort).orderByDesc(DeliveryExpressDO::getId));
        return PageResult.of(page.getRecords().stream().map(DeliveryExpressConverter::toEntity).toList(), page.getTotal());
    }
}

package com.lemnt.trade.infrastructure.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplate;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFilter;
import com.lemnt.trade.infrastructure.delivery.converter.DeliveryExpressTemplateConverter;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateDO;
import com.lemnt.trade.infrastructure.delivery.mapper.DeliveryExpressTemplateMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * trade_delivery_express_template 持久化实现。
 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateGatewayImpl implements DeliveryExpressTemplateGateway {
    private final DeliveryExpressTemplateMapper mapper;

    @Override
    public DeliveryExpressTemplate find(Long id) {
        return DeliveryExpressTemplateConverter.toEntity(mapper.selectById(id));
    }

    @Override
    public DeliveryExpressTemplate lock(Long id) {
        return DeliveryExpressTemplateConverter.toEntity(mapper.lockById(id));
    }

    @Override
    public List<DeliveryExpressTemplate> list(String name) {
        return mapper.selectList(Wrappers.<DeliveryExpressTemplateDO>lambdaQuery()
                        .like(name != null && !name.isBlank(), DeliveryExpressTemplateDO::getName, name)
                        .orderByAsc(DeliveryExpressTemplateDO::getSort).orderByDesc(DeliveryExpressTemplateDO::getId))
                .stream().map(DeliveryExpressTemplateConverter::toEntity).toList();
    }

    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) == 1;
    }

    @Override
    public DeliveryExpressTemplate insert(DeliveryExpressTemplate value) {
        var row = DeliveryExpressTemplateConverter.toDO(value);
        mapper.insert(row);
        return DeliveryExpressTemplateConverter.toEntity(row);
    }

    @Override
    public boolean update(DeliveryExpressTemplate value) {
        return mapper.updateById(DeliveryExpressTemplateConverter.toDO(value)) == 1;
    }

    @Override
    public PageResult<DeliveryExpressTemplate> page(DeliveryExpressTemplateFilter filter) {
        var page = mapper.selectPage(new Page<>(filter.pageNum(), filter.pageSize()),
                Wrappers.<DeliveryExpressTemplateDO>lambdaQuery()
                        .like(filter.name() != null && !filter.name().isBlank(), DeliveryExpressTemplateDO::getName, filter.name())
                        .orderByAsc(DeliveryExpressTemplateDO::getSort).orderByDesc(DeliveryExpressTemplateDO::getId));
        return PageResult.of(page.getRecords().stream().map(DeliveryExpressTemplateConverter::toEntity).toList(), page.getTotal());
    }
}

package com.lemnt.trade.infrastructure.delivery.gateway;

import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateFreeGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFree;
import com.lemnt.trade.infrastructure.delivery.converter.DeliveryExpressTemplateFreeConverter;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateFreeDO;
import com.lemnt.trade.infrastructure.delivery.mapper.DeliveryExpressTemplateFreeMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** trade_delivery_express_template_free 持久化实现。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateFreeGatewayImpl implements DeliveryExpressTemplateFreeGateway {
    private final DeliveryExpressTemplateFreeMapper mapper;

    @Override
    public List<DeliveryExpressTemplateFree> byTemplate(Long templateId) {
        return mapper.selectList(Wrappers.<DeliveryExpressTemplateFreeDO>lambdaQuery()
                .eq(DeliveryExpressTemplateFreeDO::getTemplateId, templateId).orderByAsc(DeliveryExpressTemplateFreeDO::getId))
                .stream().map(DeliveryExpressTemplateFreeConverter::toEntity).toList();
    }

    @Override
    public void deleteIds(List<Long> ids) {
        if (!ids.isEmpty()) {
            mapper.deleteByIds(ids);
        }
    }

    @Override
    public void deleteByTemplate(Long templateId) {
        mapper.delete(Wrappers.<DeliveryExpressTemplateFreeDO>lambdaQuery().eq(DeliveryExpressTemplateFreeDO::getTemplateId, templateId));
    }

    @Override
    public DeliveryExpressTemplateFree insert(DeliveryExpressTemplateFree value) {
        var row = DeliveryExpressTemplateFreeConverter.toDO(value);
        mapper.insert(row);
        return DeliveryExpressTemplateFreeConverter.toEntity(row);
    }

    @Override
    public boolean update(DeliveryExpressTemplateFree value) {
        return mapper.updateById(DeliveryExpressTemplateFreeConverter.toDO(value)) == 1;
    }

}

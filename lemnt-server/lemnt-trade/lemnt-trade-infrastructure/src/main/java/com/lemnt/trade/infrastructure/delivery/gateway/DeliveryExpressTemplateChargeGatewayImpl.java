package com.lemnt.trade.infrastructure.delivery.gateway;

import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateChargeGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateCharge;
import com.lemnt.trade.infrastructure.delivery.converter.DeliveryExpressTemplateChargeConverter;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO;
import com.lemnt.trade.infrastructure.delivery.mapper.DeliveryExpressTemplateChargeMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** trade_delivery_express_template_charge 持久化实现。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateChargeGatewayImpl implements DeliveryExpressTemplateChargeGateway {
    private final DeliveryExpressTemplateChargeMapper mapper;

    @Override
    public List<DeliveryExpressTemplateCharge> byTemplate(Long templateId) {
        return mapper.selectList(Wrappers.<DeliveryExpressTemplateChargeDO>lambdaQuery()
                .eq(DeliveryExpressTemplateChargeDO::getTemplateId, templateId).orderByAsc(DeliveryExpressTemplateChargeDO::getId))
                .stream().map(DeliveryExpressTemplateChargeConverter::toEntity).toList();
    }

    @Override
    public void deleteIds(List<Long> ids) {
        if (!ids.isEmpty()) {
            mapper.deleteByIds(ids);
        }
    }

    @Override
    public void deleteByTemplate(Long templateId) {
        mapper.delete(Wrappers.<DeliveryExpressTemplateChargeDO>lambdaQuery().eq(DeliveryExpressTemplateChargeDO::getTemplateId, templateId));
    }

    @Override
    public DeliveryExpressTemplateCharge insert(DeliveryExpressTemplateCharge value) {
        var row = DeliveryExpressTemplateChargeConverter.toDO(value);
        mapper.insert(row);
        return DeliveryExpressTemplateChargeConverter.toEntity(row);
    }

    @Override
    public boolean update(DeliveryExpressTemplateCharge value) {
        return mapper.updateById(DeliveryExpressTemplateChargeConverter.toDO(value)) == 1;
    }

}

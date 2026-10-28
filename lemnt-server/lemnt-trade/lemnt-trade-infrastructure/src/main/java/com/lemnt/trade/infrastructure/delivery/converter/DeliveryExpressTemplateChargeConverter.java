package com.lemnt.trade.infrastructure.delivery.converter;

import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateCharge;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO;

import com.alibaba.fastjson2.JSON;

/** 运费计费规则 与表字段转换。 */
public final class DeliveryExpressTemplateChargeConverter {
    private DeliveryExpressTemplateChargeConverter() {
    }

    /** 读取业务对象。 */
    public static DeliveryExpressTemplateCharge toEntity(DeliveryExpressTemplateChargeDO row) {
        if (row == null) {
            return null;
        }
        return new DeliveryExpressTemplateCharge(
                row.getId(),
                row.getTemplateId(),
                JSON.parseArray(row.getAreaIds(), Integer.class),
                row.getChargeMode(),
                row.getStartCount(),
                row.getStartPrice(),
                row.getExtraCount(),
                row.getExtraPrice());
    }

    /** 写入表字段，集合列按 JSON 保存。 */
    public static DeliveryExpressTemplateChargeDO toDO(DeliveryExpressTemplateCharge value) {
        var row = new DeliveryExpressTemplateChargeDO();
        row.setId(value.id());
        row.setTemplateId(value.templateId());
        row.setAreaIds(JSON.toJSONString(value.areaIds()));
        row.setChargeMode(value.chargeMode());
        row.setStartCount(value.startCount());
        row.setStartPrice(value.startPrice());
        row.setExtraCount(value.extraCount());
        row.setExtraPrice(value.extraPrice());
        return row;
    }
}

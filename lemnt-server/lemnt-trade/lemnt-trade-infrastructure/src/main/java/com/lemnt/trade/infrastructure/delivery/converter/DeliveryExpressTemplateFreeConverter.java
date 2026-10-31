package com.lemnt.trade.infrastructure.delivery.converter;

import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFree;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateFreeDO;

import com.alibaba.fastjson2.JSON;

/** 运费包邮规则 与表字段转换。 */
public final class DeliveryExpressTemplateFreeConverter {
    private DeliveryExpressTemplateFreeConverter() {
    }

    /** 读取业务对象。 */
    public static DeliveryExpressTemplateFree toEntity(DeliveryExpressTemplateFreeDO row) {
        if (row == null) {
            return null;
        }
        return new DeliveryExpressTemplateFree(
                row.getId(),
                row.getTemplateId(),
                JSON.parseArray(row.getAreaIds(), Integer.class),
                row.getFreePrice(),
                row.getFreeCount());
    }

    /** 写入表字段，集合列按 JSON 保存。 */
    public static DeliveryExpressTemplateFreeDO toDO(DeliveryExpressTemplateFree value) {
        var row = new DeliveryExpressTemplateFreeDO();
        row.setId(value.id());
        row.setTemplateId(value.templateId());
        row.setAreaIds(JSON.toJSONString(value.areaIds()));
        row.setFreePrice(value.freePrice());
        row.setFreeCount(value.freeCount());
        return row;
    }
}

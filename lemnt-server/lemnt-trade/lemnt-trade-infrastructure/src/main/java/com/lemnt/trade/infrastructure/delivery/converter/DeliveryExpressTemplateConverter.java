package com.lemnt.trade.infrastructure.delivery.converter;

import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplate;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateDO;

/** 运费模板 与表字段转换。 */
public final class DeliveryExpressTemplateConverter {
    private DeliveryExpressTemplateConverter() {
    }

    /** 读取业务对象。 */
    public static DeliveryExpressTemplate toEntity(DeliveryExpressTemplateDO row) {
        if (row == null) {
            return null;
        }
        return new DeliveryExpressTemplate(
                row.getId(),
                row.getName(),
                row.getChargeMode(),
                row.getSort());
    }

    /** 写入表字段，集合列按 JSON 保存。 */
    public static DeliveryExpressTemplateDO toDO(DeliveryExpressTemplate value) {
        var row = new DeliveryExpressTemplateDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setChargeMode(value.chargeMode());
        row.setSort(value.sort());
        return row;
    }
}

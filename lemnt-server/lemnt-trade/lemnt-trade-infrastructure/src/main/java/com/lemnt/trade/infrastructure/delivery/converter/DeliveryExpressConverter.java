package com.lemnt.trade.infrastructure.delivery.converter;

import com.lemnt.trade.domain.delivery.model.DeliveryExpress;
import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressDO;

/** 快递公司 与表字段转换。 */
public final class DeliveryExpressConverter {
    private DeliveryExpressConverter() {
    }

    /** 读取业务对象。 */
    public static DeliveryExpress toEntity(DeliveryExpressDO row) {
        if (row == null) {
            return null;
        }
        return new DeliveryExpress(
                row.getId(),
                row.getCode(),
                row.getName(),
                row.getLogo(),
                row.getSort(),
                row.getStatus());
    }

    /** 写入表字段，集合列按 JSON 保存。 */
    public static DeliveryExpressDO toDO(DeliveryExpress value) {
        var row = new DeliveryExpressDO();
        row.setId(value.id());
        row.setCode(value.code());
        row.setName(value.name());
        row.setLogo(value.logo());
        row.setSort(value.sort());
        row.setStatus(value.status());
        return row;
    }
}

package com.lemnt.trade.application.delivery.converter;

import com.lemnt.trade.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateChargeDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateFreeDTO;
import com.lemnt.trade.domain.delivery.model.DeliveryExpress;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplate;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateCharge;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFree;

/** delivery 应用输出转换。 */
public final class DeliveryDTOConverter {
    private DeliveryDTOConverter() {
    }

    /** 转换 快递公司。 */
    public static DeliveryExpressDTO toDTO(DeliveryExpress value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressDTO(
                value.id(),
                value.code(),
                value.name(),
                value.logo(),
                value.sort(),
                value.status());
    }

    /** 转换 运费模板。 */
    public static DeliveryExpressTemplateDTO toDTO(DeliveryExpressTemplate value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplateDTO(
                value.id(),
                value.name(),
                value.chargeMode(),
                value.sort());
    }

    /** 转换 运费计费规则。 */
    public static DeliveryExpressTemplateChargeDTO toDTO(DeliveryExpressTemplateCharge value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplateChargeDTO(
                value.id(),
                value.templateId(),
                value.areaIds(),
                value.chargeMode(),
                value.startCount(),
                value.startPrice(),
                value.extraCount(),
                value.extraPrice());
    }

    /** 转换 运费包邮规则。 */
    public static DeliveryExpressTemplateFreeDTO toDTO(DeliveryExpressTemplateFree value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplateFreeDTO(
                value.id(),
                value.templateId(),
                value.areaIds(),
                value.freePrice(),
                value.freeCount());
    }
}

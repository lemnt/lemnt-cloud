package com.lemnt.trade.adapter.web.delivery.converter;

import com.lemnt.trade.adapter.web.delivery.res.DeliveryChargeRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressDetailRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressPageRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressSimpleRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplateDetailRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplatePageRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryExpressTemplateSimpleRes;
import com.lemnt.trade.adapter.web.delivery.res.DeliveryFreeRes;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateChargeDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateDetailDTO;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateFreeDTO;

/** delivery Res 接口边界转换。 */
public final class DeliveryResConverter {
    private DeliveryResConverter() {
    }

    /** 转换分页响应。 */
    public static DeliveryExpressPageRes deliveryExpressPage(DeliveryExpressDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressPageRes(
                value.id(),
                value.code(),
                value.name(),
                value.logo(),
                value.sort(),
                value.status());
    }

    /** 转换详情响应。 */
    public static DeliveryExpressDetailRes deliveryExpressDetail(DeliveryExpressDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressDetailRes(
                value.id(),
                value.code(),
                value.name(),
                value.logo(),
                value.sort(),
                value.status());
    }

    /** 转换选择列表响应。 */
    public static DeliveryExpressSimpleRes deliveryExpressSimple(DeliveryExpressDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressSimpleRes(
                value.id(),
                value.code(),
                value.name());
    }

    /** 转换分页响应。 */
    public static DeliveryExpressTemplatePageRes deliveryExpressTemplatePage(DeliveryExpressTemplateDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplatePageRes(
                value.id(),
                value.name(),
                value.chargeMode(),
                value.sort());
    }

    /** 转换详情响应。 */
    public static DeliveryExpressTemplateDetailRes deliveryExpressTemplateDetail(
            DeliveryExpressTemplateDetailDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplateDetailRes(
                value.id(),
                value.name(),
                value.chargeMode(),
                value.sort(),
                value.charges().stream().map(DeliveryResConverter::charge).toList(),
                value.frees().stream().map(DeliveryResConverter::free).toList());
    }

    /** 转换选择列表响应。 */
    public static DeliveryExpressTemplateSimpleRes deliveryExpressTemplateSimple(DeliveryExpressTemplateDTO value) {
        if (value == null) {
            return null;
        }
        return new DeliveryExpressTemplateSimpleRes(
                value.id(),
                value.name());
    }

    /** 转换子规则。 */
    private static DeliveryChargeRes charge(DeliveryExpressTemplateChargeDTO value) {
        return new DeliveryChargeRes(
                value.id(),
                value.templateId(),
                value.areaIds(),
                value.chargeMode(),
                value.startCount(),
                value.startPrice(),
                value.extraCount(),
                value.extraPrice());
    }

    /** 转换子规则。 */
    private static DeliveryFreeRes free(DeliveryExpressTemplateFreeDTO value) {
        return new DeliveryFreeRes(
                value.id(),
                value.templateId(),
                value.areaIds(),
                value.freePrice(),
                value.freeCount());
    }
}

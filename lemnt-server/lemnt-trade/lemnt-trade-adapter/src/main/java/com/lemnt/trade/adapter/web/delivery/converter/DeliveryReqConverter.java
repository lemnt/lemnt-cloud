package com.lemnt.trade.adapter.web.delivery.converter;

import com.lemnt.trade.adapter.web.delivery.req.DeliveryChargeReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressCreateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressPageReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplateCreateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplatePageReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressTemplateUpdateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryExpressUpdateReq;
import com.lemnt.trade.adapter.web.delivery.req.DeliveryFreeReq;
import com.lemnt.trade.application.delivery.cmd.DeliveryChargeCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateUpdateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressUpdateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryFreeCmd;
import com.lemnt.trade.application.delivery.qry.DeliveryExpressPageQry;
import com.lemnt.trade.application.delivery.qry.DeliveryExpressTemplatePageQry;

/** delivery Req 接口边界转换。 */
public final class DeliveryReqConverter {
    private DeliveryReqConverter() {
    }

    /** 转换新增请求。 */
    public static DeliveryExpressCreateCmd deliveryExpressCreate(DeliveryExpressCreateReq req) {
        return new DeliveryExpressCreateCmd(
                req.code(),
                req.name(),
                req.logo(),
                req.sort(),
                req.status());
    }

    /** 转换修改请求。 */
    public static DeliveryExpressUpdateCmd deliveryExpressUpdate(Long id, DeliveryExpressUpdateReq req) {
        return new DeliveryExpressUpdateCmd(
                id,
                req.code(),
                req.name(),
                req.logo(),
                req.sort(),
                req.status());
    }

    /** 转换分页请求。 */
    public static DeliveryExpressPageQry deliveryExpressPage(DeliveryExpressPageReq req) {
        return new DeliveryExpressPageQry(
                req.name(),
                req.status(),
                req.pageNum(),
                req.pageSize());
    }

    /** 转换新增请求。 */
    public static DeliveryExpressTemplateCreateCmd deliveryExpressTemplateCreate(DeliveryExpressTemplateCreateReq req) {
        return new DeliveryExpressTemplateCreateCmd(
                req.name(),
                req.chargeMode(),
                req.sort(),
                req.charges().stream().map(DeliveryReqConverter::charge).toList(),
                req.frees().stream().map(DeliveryReqConverter::free).toList());
    }

    /** 转换修改请求。 */
    public static DeliveryExpressTemplateUpdateCmd deliveryExpressTemplateUpdate(
            Long id,
            DeliveryExpressTemplateUpdateReq req) {
        return new DeliveryExpressTemplateUpdateCmd(
                id,
                req.name(),
                req.chargeMode(),
                req.sort(),
                req.charges().stream().map(DeliveryReqConverter::charge).toList(),
                req.frees().stream().map(DeliveryReqConverter::free).toList());
    }

    /** 转换分页请求。 */
    public static DeliveryExpressTemplatePageQry deliveryExpressTemplatePage(DeliveryExpressTemplatePageReq req) {
        return new DeliveryExpressTemplatePageQry(
                req.name(),
                req.pageNum(),
                req.pageSize());
    }

    /** 转换子规则。 */
    private static DeliveryChargeCmd charge(DeliveryChargeReq value) {
        return new DeliveryChargeCmd(
                value.id(),
                value.areaIds(),
                value.startCount(),
                value.startPrice(),
                value.extraCount(),
                value.extraPrice());
    }

    /** 转换子规则。 */
    private static DeliveryFreeCmd free(DeliveryFreeReq value) {
        return new DeliveryFreeCmd(
                value.id(),
                value.areaIds(),
                value.freePrice(),
                value.freeCount());
    }
}

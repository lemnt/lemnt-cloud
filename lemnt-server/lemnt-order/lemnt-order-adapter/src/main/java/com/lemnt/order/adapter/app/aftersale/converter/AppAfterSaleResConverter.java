package com.lemnt.order.adapter.app.aftersale.converter;

import com.lemnt.order.adapter.app.aftersale.res.AppAfterSaleItemRes;
import com.lemnt.order.adapter.app.aftersale.res.AppAfterSaleRes;
import com.lemnt.order.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.order.application.aftersale.dto.AfterSaleItemDTO;

/** 会员售后接口响应转换。 */
public final class AppAfterSaleResConverter {
    private AppAfterSaleResConverter() {}

    /** 转换售后详情或列表项。 */
    public static AppAfterSaleRes afterSale(AfterSaleDTO value) {
        return new AppAfterSaleRes(
                value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.paymentNo(),
                value.orderItemId(),
                value.quantity(),
                value.type(),
                value.amount(),
                value.reason(),
                value.status(),
                value.decisionReason(),
                value.returnCarrierCode(),
                value.returnTrackingNo(),
                value.requestedAt(),
                value.decidedAt(),
                value.returnShippedAt(),
                value.returnReceivedAt(),
                value.refundTime(),
                value.spuId(),
                value.spuName(),
                value.specs() == null
                        ? null
                        : value.specs().stream()
                                .map(
                                        property ->
                                                new AppAfterSaleRes.Spec(
                                                        property.specId(),
                                                        property.specName(),
                                                        property.specValueId(),
                                                        property.specValueName()))
                                .toList(),
                value.picUrl());
    }

    /** 转换可售后额度。 */
    public static AppAfterSaleItemRes item(AfterSaleItemDTO value) {
        return new AppAfterSaleItemRes(
                value.orderItemId(),
                value.productId(),
                value.productName(),
                value.quantity(),
                value.refundableQuantity(),
                value.refundableAmount(),
                value.spuId(),
                value.spuName(),
                value.specs() == null
                        ? null
                        : value.specs().stream()
                                .map(
                                        property ->
                                                new AppAfterSaleItemRes.Spec(
                                                        property.specId(),
                                                        property.specName(),
                                                        property.specValueId(),
                                                        property.specValueName()))
                                .toList(),
                value.picUrl(),
                value.refundedQuantity());
    }
}

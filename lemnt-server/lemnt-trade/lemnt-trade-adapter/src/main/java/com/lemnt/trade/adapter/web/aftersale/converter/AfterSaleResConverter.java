package com.lemnt.trade.adapter.web.aftersale.converter;

import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleAgreeRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleCreateRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleDetailRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleItemRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSalePageRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleReceiveRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleRefuseRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleRejectRes;
import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleReturnRes;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.dto.AfterSaleItemDTO;

/** 售后接口独立响应转换。 */
public final class AfterSaleResConverter {
    private AfterSaleResConverter() {
    }

    /** 转换售后申请接口响应。 */
    public static AfterSaleCreateRes create(AfterSaleDTO value) {
        return new AfterSaleCreateRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后分页接口响应。 */
    public static AfterSalePageRes page(AfterSaleDTO value) {
        return new AfterSalePageRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后详情接口响应。 */
    public static AfterSaleDetailRes detail(AfterSaleDTO value) {
        return new AfterSaleDetailRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后同意接口响应。 */
    public static AfterSaleAgreeRes agree(AfterSaleDTO value) {
        return new AfterSaleAgreeRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后拒绝接口响应。 */
    public static AfterSaleRejectRes reject(AfterSaleDTO value) {
        return new AfterSaleRejectRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后登记退货接口响应。 */
    public static AfterSaleReturnRes returned(AfterSaleDTO value) {
        return new AfterSaleReturnRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后确认收货接口响应。 */
    public static AfterSaleReceiveRes receive(AfterSaleDTO value) {
        return new AfterSaleReceiveRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /** 转换售后拒收接口响应。 */
    public static AfterSaleRefuseRes refuse(AfterSaleDTO value) {
        return new AfterSaleRefuseRes(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
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
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }
    /** 转换可售后商品额度。 */
    public static AfterSaleItemRes item(AfterSaleItemDTO value) {
        return new AfterSaleItemRes(value.orderItemId(),
                value.productId(),
                value.productName(),
                value.quantity(),
                value.refundableQuantity(),
                value.refundableAmount(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }
}

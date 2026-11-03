package com.lemnt.mall.adapter.app.settlement.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 服务端缓存结算单，有效期15分钟。
 */
@Schema(description = "服务端缓存结算单，有效期15分钟")
public record SettlementRes(
        @Schema(description = "结算单编号，提单只提交此编号与requestNo") String settlementId,
        @Schema(description = "失效时间，ISO-8601 UTC") String expiresAt,
        @Schema(description = "商品快照") List<SettlementItemRes> items,
        @Schema(description = "商品原金额，人民币元") BigDecimal originalAmount,
        @Schema(description = "优惠金额，人民币元") BigDecimal discountAmount,
        @Schema(description = "应付金额，人民币元") BigDecimal payableAmount,
        @Schema(description = "所选券实例编号") Long couponId,
        @Schema(description = "本单抵扣积分数") long usePoint,
        @Schema(description = "支付成功后赠送积分数") long givePoint,
        @Schema(description = "已冻结的配送快照") SettlementDeliveryRes delivery,
        @Schema(description = "满减活动是否包邮") boolean freeDelivery,
        @Schema(description = "支付成功后赠送的优惠券模板及数量") Map<Long, Integer> giveCouponTemplateCounts)
        implements Serializable {}

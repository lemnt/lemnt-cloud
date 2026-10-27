package com.lemnt.mall.application.promotion.dto;

/**
 * 营销阶梯数据传输对象。
 */
public record PromotionTierDTO(
        long thresholdFen,
        long discountFen
) implements java.io.Serializable {}

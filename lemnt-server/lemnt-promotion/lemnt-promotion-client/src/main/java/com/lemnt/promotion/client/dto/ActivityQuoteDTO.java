package com.lemnt.promotion.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
/** 活动原价、行实付和积分冻结结果，由订单服务再附加配送。 */
public record ActivityQuoteDTO(String activityType, Long activityId, Long recordId, Long headId, Long skuId, Integer quantity, BigDecimal activityPrice, long usePoint, PromotionQuoteDTO quote, List<Line> lines) implements Serializable { public record Line(Long skuId,Integer quantity,BigDecimal activityPrice,long usePoint) implements Serializable {} }

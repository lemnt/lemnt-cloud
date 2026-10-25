package com.lemnt.trade.domain.promotion.model;

public record PromotionCounter(long held, long used, Long globalLimit, Long memberLimit) {}

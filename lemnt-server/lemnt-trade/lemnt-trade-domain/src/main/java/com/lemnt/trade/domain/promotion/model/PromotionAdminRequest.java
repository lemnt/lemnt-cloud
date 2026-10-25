package com.lemnt.trade.domain.promotion.model;

public record PromotionAdminRequest(
        String action, String fingerprint, PromotionAdminReceipt receipt) {}

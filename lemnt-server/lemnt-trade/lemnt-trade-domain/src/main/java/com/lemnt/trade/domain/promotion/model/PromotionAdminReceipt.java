package com.lemnt.trade.domain.promotion.model;

public record PromotionAdminReceipt(
        long id,
        long revision,
        PromotionRoot.State state,
        Integer activeVersion,
        int lastVersion) {}

package com.lemnt.trade.application.promotion.dto;

public record PromotionDetailDTO(
        long id,
        int revision,
        String state,
        Integer activeVersion,
        String code,
        PromotionDraftDTO draft,
        PromotionDraftDTO activeRule,
        String timePhase) {}

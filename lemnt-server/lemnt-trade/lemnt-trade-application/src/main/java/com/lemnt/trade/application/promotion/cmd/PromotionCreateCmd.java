package com.lemnt.trade.application.promotion.cmd;

public record PromotionCreateCmd(
        long operatorId,
        String requestNo,
        String code,
        com.lemnt.trade.domain.promotion.model.PromotionDraft draft) {}

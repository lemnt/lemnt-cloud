package com.lemnt.trade.application.promotion.cmd;

public record PromotionDraftSaveCmd(
        long operatorId,
        String requestNo,
        long id,
        int expectedRevision,
        com.lemnt.trade.domain.promotion.model.PromotionDraft draft) {}

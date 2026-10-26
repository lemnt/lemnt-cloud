package com.lemnt.trade.application.promotion.cmd;

public record PromotionStateChangeCmd(
        long operatorId,
        String requestNo,
        long id,
        int expectedRevision,
        com.lemnt.trade.domain.promotion.model.PromotionRoot.State target) {}

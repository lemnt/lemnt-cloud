package com.lemnt.trade.application.promotion.cmd;

public record PromotionPublishCmd(
        long operatorId, String requestNo, long id, int expectedRevision) {}

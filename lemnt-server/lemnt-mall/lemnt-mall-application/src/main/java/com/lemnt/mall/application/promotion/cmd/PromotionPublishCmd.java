package com.lemnt.mall.application.promotion.cmd;

/**
 * 营销发布命令。
 */
public record PromotionPublishCmd(
        long operatorId,
        String requestNo,
        long id,
        int expectedRevision
) implements java.io.Serializable {}

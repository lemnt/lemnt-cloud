package com.lemnt.mall.application.promotion.cmd;

import com.lemnt.mall.domain.promotion.model.PromotionDraft;

/**
 * 营销创建命令。
 */
public record PromotionCreateCmd(
        long operatorId,
        String requestNo,
        String code,
        PromotionDraft draft
) implements java.io.Serializable {}

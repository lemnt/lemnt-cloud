package com.lemnt.mall.application.promotion.cmd;

import com.lemnt.mall.domain.promotion.model.PromotionDraft;

/**
 * 营销草稿保存命令。
 */
public record PromotionDraftSaveCmd(
        long operatorId,
        String requestNo,
        long id,
        int expectedRevision,
        PromotionDraft draft
) implements java.io.Serializable {}

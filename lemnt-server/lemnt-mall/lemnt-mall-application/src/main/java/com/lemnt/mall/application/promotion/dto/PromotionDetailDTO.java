package com.lemnt.mall.application.promotion.dto;

/**
 * 营销详情数据传输对象。
 */
public record PromotionDetailDTO(
        long id,
        int revision,
        String state,
        Integer activeVersion,
        String code,
        PromotionDraftDTO draft,
        PromotionDraftDTO activeRule,
        String timePhase
) implements java.io.Serializable {}

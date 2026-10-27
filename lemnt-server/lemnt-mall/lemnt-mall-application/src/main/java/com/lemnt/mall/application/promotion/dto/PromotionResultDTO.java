package com.lemnt.mall.application.promotion.dto;

/**
 * 营销结果数据传输对象。
 */
public record PromotionResultDTO(
        long id,
        int revision,
        String state,
        Integer activeVersion
) implements java.io.Serializable {}

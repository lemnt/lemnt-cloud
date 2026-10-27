package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/**
 * 营销分页数据传输对象。
 */
public record PromotionPageDTO(
        List<PromotionResultDTO> records,
        long total
) implements java.io.Serializable {}

package com.lemnt.mall.application.store.dto;

import java.util.List;

/**
 * 营销分页数据传输对象。
 */
public record PromotionPageDTO(
        List<CouponOfferDTO> records,
        long total
) implements java.io.Serializable {}

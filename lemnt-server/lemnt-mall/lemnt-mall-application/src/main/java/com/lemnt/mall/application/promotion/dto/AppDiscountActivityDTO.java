package com.lemnt.mall.application.promotion.dto;

import com.lemnt.mall.domain.promotion.model.DiscountProduct;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 小程序限时折扣活动详情。 */
public record AppDiscountActivityDTO(
        Long id,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<DiscountProduct> products)
        implements Serializable {
    public AppDiscountActivityDTO {
        products = products == null ? List.of() : List.copyOf(products);
    }
}

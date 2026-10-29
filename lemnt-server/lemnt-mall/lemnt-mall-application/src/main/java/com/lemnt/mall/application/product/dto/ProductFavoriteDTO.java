package com.lemnt.mall.application.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Favorite及当前商品资料，缺失资料保留null。 */
public record ProductFavoriteDTO(Long id, Long userId, Long spuId, String spuName, String picUrl, BigDecimal price,
        LocalDateTime createTime) {}

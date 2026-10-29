package com.lemnt.mall.application.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** BrowseHistory及当前商品资料，缺失资料保留null。 */
public record ProductBrowseHistoryDTO(Long id, Long userId, Long spuId, String spuName, String picUrl, BigDecimal price,
        boolean userDeleted, LocalDateTime createTime) {}

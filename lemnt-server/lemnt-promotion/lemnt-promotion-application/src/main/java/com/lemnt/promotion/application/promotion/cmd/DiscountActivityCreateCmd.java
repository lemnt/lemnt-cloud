package com.lemnt.promotion.application.promotion.cmd;

import com.lemnt.promotion.domain.promotion.model.DiscountProduct;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建活动命令。
 */
public record DiscountActivityCreateCmd(
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    List<DiscountProduct> products) {
}

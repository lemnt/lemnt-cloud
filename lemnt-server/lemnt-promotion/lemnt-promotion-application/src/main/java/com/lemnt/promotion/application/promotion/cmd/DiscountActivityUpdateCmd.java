package com.lemnt.promotion.application.promotion.cmd;

import com.lemnt.promotion.domain.promotion.model.DiscountProduct;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改活动命令。
 */
public record DiscountActivityUpdateCmd(
    Long id,
    String name,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    List<DiscountProduct> products) {
}

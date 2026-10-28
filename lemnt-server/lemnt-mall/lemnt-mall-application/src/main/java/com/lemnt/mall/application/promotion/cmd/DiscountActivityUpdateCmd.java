package com.lemnt.mall.application.promotion.cmd;

import com.lemnt.mall.domain.promotion.model.DiscountProduct;

import java.time.LocalDateTime;
import java.util.List;

/** 修改活动命令。 */
public record DiscountActivityUpdateCmd(
        Long id,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String remark,
        List<DiscountProduct> products
) {}

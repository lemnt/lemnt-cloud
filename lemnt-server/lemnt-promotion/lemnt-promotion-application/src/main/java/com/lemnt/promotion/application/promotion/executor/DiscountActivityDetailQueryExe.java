package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.DiscountActivityDTO;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Detail执行器。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityDetailQueryExe {
    private final ActivityManagementService service;

    public DiscountActivityDTO execute(Long id) {
        return service.detailDiscount(id);
    }
}

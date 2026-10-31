package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Delete执行器。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityDeleteCmdExe {
    private final ActivityManagementService service;

    public Boolean execute(Long id) {
        return service.deleteDiscount(id);
    }
}

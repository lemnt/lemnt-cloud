package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Close执行器。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityCloseCmdExe {
    private final ActivityManagementService service;

    public Boolean execute(Long id) {
        return service.closeDiscount(id);
    }
}

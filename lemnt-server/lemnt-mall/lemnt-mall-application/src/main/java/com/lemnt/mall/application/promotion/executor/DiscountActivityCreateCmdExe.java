package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Create执行器。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityCreateCmdExe {
    private final ActivityManagementService service;

    public Long execute(DiscountActivityCreateCmd cmd) {
        return service.createDiscount(cmd);
    }
}

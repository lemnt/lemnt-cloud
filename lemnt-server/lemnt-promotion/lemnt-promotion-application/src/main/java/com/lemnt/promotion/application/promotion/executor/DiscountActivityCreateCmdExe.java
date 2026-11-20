package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 活动Create执行器。
 */
@Component
@RequiredArgsConstructor
public class DiscountActivityCreateCmdExe {
    private final ActivityManagementService service;

    public Long execute(DiscountActivityCreateCmd cmd) {
        return service.createDiscount(cmd);
    }
}

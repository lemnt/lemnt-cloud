package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiscountActivityUpdateCmd;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Update执行器。 */
@Component
@RequiredArgsConstructor
public class DiscountActivityUpdateCmdExe {
    private final ActivityManagementService service;

    public Boolean execute(DiscountActivityUpdateCmd cmd) {
        return service.updateDiscount(cmd);
    }
}

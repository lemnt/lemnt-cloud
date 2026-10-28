package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.RewardActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Create执行器。 */
@Component
@RequiredArgsConstructor
public class RewardActivityCreateCmdExe {
    private final ActivityManagementService service;

    public Long execute(RewardActivityCreateCmd cmd) {
        return service.createReward(cmd);
    }
}

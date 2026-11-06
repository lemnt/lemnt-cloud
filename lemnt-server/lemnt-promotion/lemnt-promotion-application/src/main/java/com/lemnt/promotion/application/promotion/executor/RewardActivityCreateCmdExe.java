package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.RewardActivityCreateCmd;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 活动Create执行器。
 */
@Component
@RequiredArgsConstructor
public class RewardActivityCreateCmdExe {
    private final ActivityManagementService service;

    public Long execute(RewardActivityCreateCmd cmd) {
        return service.createReward(cmd);
    }
}

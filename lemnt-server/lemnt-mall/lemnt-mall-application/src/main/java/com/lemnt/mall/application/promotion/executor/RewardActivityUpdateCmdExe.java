package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.RewardActivityUpdateCmd;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Update执行器。 */
@Component
@RequiredArgsConstructor
public class RewardActivityUpdateCmdExe {
    private final ActivityManagementService service;

    public Boolean execute(RewardActivityUpdateCmd cmd) {
        return service.updateReward(cmd);
    }
}

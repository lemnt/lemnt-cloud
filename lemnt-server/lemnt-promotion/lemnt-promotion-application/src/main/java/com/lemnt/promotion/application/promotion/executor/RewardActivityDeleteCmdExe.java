package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Delete执行器。 */
@Component
@RequiredArgsConstructor
public class RewardActivityDeleteCmdExe {
    private final ActivityManagementService service;

    public Boolean execute(Long id) {
        return service.deleteReward(id);
    }
}

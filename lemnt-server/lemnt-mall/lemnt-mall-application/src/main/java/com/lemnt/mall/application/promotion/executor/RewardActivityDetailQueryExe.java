package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.RewardActivityDTO;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Detail执行器。 */
@Component
@RequiredArgsConstructor
public class RewardActivityDetailQueryExe {
    private final ActivityManagementService service;

    public RewardActivityDTO execute(Long id) {
        return service.detailReward(id);
    }
}

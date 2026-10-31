package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动成团数量执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationSuccessCountQueryExe {
    private final CombinationActivityManagementService service;

    public long execute(Long activityId) {
        return service.successCount(activityId);
    }
}

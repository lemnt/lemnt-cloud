package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 发起砍价执行器。 */
@Component
@RequiredArgsConstructor
public class BargainRecordCreateCmdExe {
    private final BargainRecordService service;

    public Long execute(Long userId, Long activityId) {
        return service.create(userId, activityId);
    }
}

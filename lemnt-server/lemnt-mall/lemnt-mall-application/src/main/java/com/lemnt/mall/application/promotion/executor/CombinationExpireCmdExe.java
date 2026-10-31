package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团过期处理执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationExpireCmdExe {
    private final CombinationRecordService service;

    public int execute() {
        return service.expire();
    }
}

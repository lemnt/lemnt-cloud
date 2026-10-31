package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价到期失败执行器。 */
@Component
@RequiredArgsConstructor
public class BargainExpireCmdExe {
    private final BargainRecordService service;

    public int execute() {
        return service.expire();
    }
}

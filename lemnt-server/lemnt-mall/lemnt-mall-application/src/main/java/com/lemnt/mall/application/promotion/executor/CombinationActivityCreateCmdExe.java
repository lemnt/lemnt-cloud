package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动新增执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityCreateCmdExe {
    private final CombinationActivityManagementService service;

    public Long execute(CombinationActivityCreateCmd cmd) {
        return service.create(cmd);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 积分商城活动新增执行器。 */
@Component
@RequiredArgsConstructor
public class PointActivityCreateCmdExe {
    private final PointActivityManagementService service;

    public Long execute(PointActivityCreateCmd cmd) {
        return service.create(cmd);
    }
}

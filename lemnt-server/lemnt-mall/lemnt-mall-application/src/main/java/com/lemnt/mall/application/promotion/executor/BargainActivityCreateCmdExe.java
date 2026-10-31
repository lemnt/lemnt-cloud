package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动新增执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityCreateCmdExe {
    private final BargainActivityManagementService service;

    public Long execute(BargainActivityCreateCmd cmd) {
        return service.create(cmd);
    }
}

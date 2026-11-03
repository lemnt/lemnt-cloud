package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.promotion.application.promotion.service.BargainActivityManagementService;

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

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.BargainActivityUpdateCmd;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动修改执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityUpdateCmdExe {
    private final BargainActivityManagementService service;

    public Boolean execute(BargainActivityUpdateCmd cmd) {
        return service.update(cmd);
    }
}

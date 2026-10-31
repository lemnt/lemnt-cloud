package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.CombinationActivityUpdateCmd;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动修改执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityUpdateCmdExe {
    private final CombinationActivityManagementService service;

    public Boolean execute(CombinationActivityUpdateCmd cmd) {
        return service.update(cmd);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PointActivityUpdateCmd;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 积分商城活动修改执行器。 */
@Component
@RequiredArgsConstructor
public class PointActivityUpdateCmdExe {
    private final PointActivityManagementService service;

    public Boolean execute(PointActivityUpdateCmd cmd) {
        return service.update(cmd);
    }
}

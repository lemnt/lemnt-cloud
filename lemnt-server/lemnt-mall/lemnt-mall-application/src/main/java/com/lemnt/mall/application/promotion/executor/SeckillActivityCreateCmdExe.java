package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀活动新增执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityCreateCmdExe {
    private final SeckillActivityManagementService service;

    public Long execute(SeckillActivityCreateCmd cmd) {
        return service.create(cmd);
    }
}

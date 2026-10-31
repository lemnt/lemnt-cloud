package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.SeckillActivityUpdateCmd;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀活动修改执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityUpdateCmdExe {
    private final SeckillActivityManagementService service;

    public Boolean execute(SeckillActivityUpdateCmd cmd) {
        return service.update(cmd);
    }
}

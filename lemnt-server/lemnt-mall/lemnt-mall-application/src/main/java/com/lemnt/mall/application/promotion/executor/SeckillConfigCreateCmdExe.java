package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀时段新增执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigCreateCmdExe {
    private final SeckillConfigManagementService service;

    public Long execute(SeckillConfigCreateCmd cmd) {
        return service.create(cmd);
    }
}

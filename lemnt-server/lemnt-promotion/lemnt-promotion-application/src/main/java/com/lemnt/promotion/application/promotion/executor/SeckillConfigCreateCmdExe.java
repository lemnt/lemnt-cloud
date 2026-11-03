package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.promotion.application.promotion.service.SeckillConfigManagementService;

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

package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.SeckillConfigUpdateCmd;
import com.lemnt.promotion.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 秒杀时段修改执行器。
 */
@Component
@RequiredArgsConstructor
public class SeckillConfigUpdateCmdExe {
    private final SeckillConfigManagementService service;

    public Boolean execute(SeckillConfigUpdateCmd cmd) {
        return service.update(cmd);
    }
}

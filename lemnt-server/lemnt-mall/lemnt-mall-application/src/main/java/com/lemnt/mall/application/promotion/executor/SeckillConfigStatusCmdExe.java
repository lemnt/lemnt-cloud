package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀时段状态修改执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigStatusCmdExe {
    private final SeckillConfigManagementService service;

    public Boolean execute(Long id, Integer status) {
        return service.updateStatus(id, status);
    }
}

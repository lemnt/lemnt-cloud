package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀时段删除执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigDeleteCmdExe {
    private final SeckillConfigManagementService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

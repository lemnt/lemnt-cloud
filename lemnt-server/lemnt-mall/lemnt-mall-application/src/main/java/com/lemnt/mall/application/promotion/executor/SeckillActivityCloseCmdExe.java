package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀活动关闭执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityCloseCmdExe {
    private final SeckillActivityManagementService service;

    public Boolean execute(Long id) {
        return service.close(id);
    }
}

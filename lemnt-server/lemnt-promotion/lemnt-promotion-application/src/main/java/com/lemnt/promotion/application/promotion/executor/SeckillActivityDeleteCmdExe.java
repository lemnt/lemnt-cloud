package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 秒杀活动删除执行器。
 */
@Component
@RequiredArgsConstructor
public class SeckillActivityDeleteCmdExe {
    private final SeckillActivityManagementService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

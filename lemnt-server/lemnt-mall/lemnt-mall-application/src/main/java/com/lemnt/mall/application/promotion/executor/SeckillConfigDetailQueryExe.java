package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀时段详情执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigDetailQueryExe {
    private final SeckillConfigManagementService service;

    public SeckillConfigDTO execute(Long id) {
        return service.detail(id);
    }
}

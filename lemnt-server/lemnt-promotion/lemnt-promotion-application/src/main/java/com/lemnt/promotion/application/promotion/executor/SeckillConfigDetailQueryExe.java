package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.promotion.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 秒杀时段详情执行器。
 */
@Component
@RequiredArgsConstructor
public class SeckillConfigDetailQueryExe {
    private final SeckillConfigManagementService service;

    public SeckillConfigDTO execute(Long id) {
        return service.detail(id);
    }
}

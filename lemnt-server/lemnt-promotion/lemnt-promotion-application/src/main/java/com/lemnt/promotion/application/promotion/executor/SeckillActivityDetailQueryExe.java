package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.promotion.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityDetailQueryExe {
    private final SeckillActivityManagementService service;

    public SeckillActivityDTO execute(Long id) {
        return service.detail(id);
    }
}

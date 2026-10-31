package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;

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

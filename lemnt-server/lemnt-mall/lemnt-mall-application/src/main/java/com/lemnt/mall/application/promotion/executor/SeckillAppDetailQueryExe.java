package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.SeckillAppDetailDTO;
import com.lemnt.mall.application.promotion.service.SeckillAppQueryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 秒杀活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillAppDetailQueryExe {
    private final SeckillAppQueryService service;

    public SeckillAppDetailDTO execute(Long id) {
        return service.detail(id);
    }
}

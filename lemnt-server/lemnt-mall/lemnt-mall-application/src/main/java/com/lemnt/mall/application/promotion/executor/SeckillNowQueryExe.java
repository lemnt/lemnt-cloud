package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.SeckillNowDTO;
import com.lemnt.mall.application.promotion.service.SeckillAppQueryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 当前秒杀活动执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillNowQueryExe {
    private final SeckillAppQueryService service;

    public SeckillNowDTO execute() {
        return service.now();
    }
}

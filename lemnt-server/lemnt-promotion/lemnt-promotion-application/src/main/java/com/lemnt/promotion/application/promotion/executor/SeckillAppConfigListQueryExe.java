package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.promotion.application.promotion.service.SeckillAppQueryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** App 秒杀时段列表执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillAppConfigListQueryExe {
    private final SeckillAppQueryService service;

    public List<SeckillConfigDTO> execute() {
        return service.configList();
    }
}

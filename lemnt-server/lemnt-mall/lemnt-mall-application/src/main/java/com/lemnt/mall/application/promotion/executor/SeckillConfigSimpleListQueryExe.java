package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 开启状态秒杀时段列表执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigSimpleListQueryExe {
    private final SeckillConfigManagementService service;

    public List<SeckillConfigDTO> execute() {
        return service.simpleList();
    }
}

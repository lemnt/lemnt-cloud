package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.qry.SeckillConfigPageQry;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀时段分页执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillConfigPageQueryExe {
    private final SeckillConfigManagementService service;

    public PageResult<SeckillConfigDTO> execute(SeckillConfigPageQry qry) {
        return service.page(qry);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.mall.application.promotion.qry.SeckillActivityPageQry;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 秒杀活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillActivityPageQueryExe {
    private final SeckillActivityManagementService service;

    public PageResult<SeckillActivityDTO> execute(SeckillActivityPageQry qry) {
        return service.page(qry);
    }
}

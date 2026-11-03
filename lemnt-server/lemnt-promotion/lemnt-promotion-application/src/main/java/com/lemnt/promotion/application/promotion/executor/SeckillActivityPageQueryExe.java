package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.promotion.application.promotion.qry.SeckillActivityPageQry;
import com.lemnt.promotion.application.promotion.service.SeckillActivityManagementService;

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

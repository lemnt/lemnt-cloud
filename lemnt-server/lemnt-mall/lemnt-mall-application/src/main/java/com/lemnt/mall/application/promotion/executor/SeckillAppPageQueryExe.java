package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.SeckillAppActivityDTO;
import com.lemnt.mall.application.promotion.qry.SeckillAppActivityPageQry;
import com.lemnt.mall.application.promotion.service.SeckillAppQueryService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 秒杀活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class SeckillAppPageQueryExe {
    private final SeckillAppQueryService service;

    public PageResult<SeckillAppActivityDTO> execute(SeckillAppActivityPageQry qry) {
        return service.page(qry);
    }
}

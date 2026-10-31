package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.PointActivityDTO;
import com.lemnt.mall.application.promotion.qry.PointActivityPageQry;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 积分商城活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class PointActivityPageQueryExe {
    private final PointActivityManagementService service;

    public PageResult<PointActivityDTO> execute(PointActivityPageQry qry) {
        return service.page(qry);
    }
}

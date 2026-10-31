package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.mall.application.promotion.qry.CombinationActivityPageQry;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityPageQueryExe {
    private final CombinationActivityManagementService service;

    public PageResult<CombinationActivityDTO> execute(CombinationActivityPageQry qry) {
        return service.page(qry);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.qry.BargainActivityPageQry;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityPageQueryExe {
    private final BargainActivityManagementService service;

    public PageResult<BargainActivityDTO> execute(BargainActivityPageQry qry) {
        return service.page(qry);
    }
}

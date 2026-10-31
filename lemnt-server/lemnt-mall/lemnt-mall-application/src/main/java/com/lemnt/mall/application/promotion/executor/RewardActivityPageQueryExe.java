package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.RewardActivityDTO;
import com.lemnt.mall.application.promotion.qry.RewardActivityPageQry;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动Page执行器。 */
@Component
@RequiredArgsConstructor
public class RewardActivityPageQueryExe {
    private final ActivityManagementService service;

    public PageResult<RewardActivityDTO> execute(RewardActivityPageQry query) {
        return service.pageReward(new ActivityFilter(query.name(), query.status(), query.pageNum(), query.pageSize()));
    }
}

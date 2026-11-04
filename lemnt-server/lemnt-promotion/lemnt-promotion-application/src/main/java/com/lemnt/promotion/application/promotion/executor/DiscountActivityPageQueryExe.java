package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.promotion.dto.DiscountActivityDTO;
import com.lemnt.promotion.application.promotion.qry.DiscountActivityPageQry;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;
import com.lemnt.promotion.domain.promotion.model.ActivityFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 活动Page执行器。
 */
@Component
@RequiredArgsConstructor
public class DiscountActivityPageQueryExe {
    private final ActivityManagementService service;

    public PageResult<DiscountActivityDTO> execute(DiscountActivityPageQry query) {
        return service.pageDiscount(
            new ActivityFilter(
                query.name(), query.status(), query.pageNum(), query.pageSize()));
    }
}

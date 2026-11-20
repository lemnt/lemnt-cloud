package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.promotion.dto.PointActivityDTO;
import com.lemnt.promotion.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * App 积分商城活动分页执行器。
 */
@Component
@RequiredArgsConstructor
public class PointAppPageQueryExe {
    private final PointActivityManagementService service;

    public PageResult<PointActivityDTO> execute(int pageNum, int pageSize) {
        return service.appPage(pageNum, pageSize);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.PointActivityDTO;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 积分商城活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class PointActivityDetailQueryExe {
    private final PointActivityManagementService service;

    public PointActivityDTO execute(Long id) {
        return service.detail(id);
    }
}

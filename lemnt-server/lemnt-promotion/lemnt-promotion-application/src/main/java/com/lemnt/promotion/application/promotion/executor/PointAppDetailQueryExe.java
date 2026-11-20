package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.PointActivityDTO;
import com.lemnt.promotion.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * App 积分商城活动详情执行器。
 */
@Component
@RequiredArgsConstructor
public class PointAppDetailQueryExe {
    private final PointActivityManagementService service;

    public PointActivityDTO execute(Long id) {
        return service.appDetail(id);
    }
}

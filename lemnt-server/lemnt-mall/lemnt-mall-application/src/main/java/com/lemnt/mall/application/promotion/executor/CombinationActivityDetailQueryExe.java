package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityDetailQueryExe {
    private final CombinationActivityManagementService service;

    public CombinationActivityDTO execute(Long id) {
        return service.detail(id);
    }
}

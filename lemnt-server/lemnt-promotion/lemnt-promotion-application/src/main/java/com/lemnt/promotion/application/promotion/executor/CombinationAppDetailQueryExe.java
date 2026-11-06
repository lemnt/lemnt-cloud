package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.promotion.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * App 拼团活动详情执行器。
 */
@Component
@RequiredArgsConstructor
public class CombinationAppDetailQueryExe {
    private final CombinationActivityManagementService service;

    public CombinationActivityDTO execute(Long id) {
        return service.appDetail(id);
    }
}

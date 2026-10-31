package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityDetailQueryExe {
    private final BargainActivityManagementService service;

    public BargainActivityDTO execute(Long id) {
        return service.detail(id);
    }
}

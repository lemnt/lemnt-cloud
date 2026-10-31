package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 砍价活动详情执行器。 */
@Component
@RequiredArgsConstructor
public class BargainAppDetailQueryExe {
    private final BargainActivityManagementService service;

    public BargainActivityDTO execute(Long id) {
        return service.appDetail(id);
    }
}

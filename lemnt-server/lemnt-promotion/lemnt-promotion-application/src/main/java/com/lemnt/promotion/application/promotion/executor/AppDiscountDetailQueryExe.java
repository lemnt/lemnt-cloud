package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.AppDiscountActivityDTO;
import com.lemnt.promotion.application.promotion.service.AppPromotionHubService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序限时折扣详情执行器。 */
@Component
@RequiredArgsConstructor
public class AppDiscountDetailQueryExe {
    private final AppPromotionHubService service;

    public AppDiscountActivityDTO execute(Long id) {
        return service.discount(id);
    }
}

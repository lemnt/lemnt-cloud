package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.AppDiscountActivityDTO;
import com.lemnt.mall.application.promotion.service.AppPromotionHubService;

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

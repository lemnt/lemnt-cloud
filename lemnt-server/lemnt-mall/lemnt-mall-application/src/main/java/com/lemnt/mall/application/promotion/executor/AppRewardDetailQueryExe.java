package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.AppRewardActivityDTO;
import com.lemnt.mall.application.promotion.service.AppPromotionHubService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序满减送详情执行器。 */
@Component
@RequiredArgsConstructor
public class AppRewardDetailQueryExe {
    private final AppPromotionHubService service;

    public AppRewardActivityDTO execute(Long id) {
        return service.reward(id);
    }
}

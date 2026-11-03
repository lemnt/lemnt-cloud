package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.AppPromotionSectionDTO;
import com.lemnt.promotion.application.promotion.service.AppPromotionHubService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序活动聚合查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppPromotionHubQueryExe {
    private final AppPromotionHubService service;

    public List<AppPromotionSectionDTO> execute(Integer limit) {
        return service.sections(limit);
    }
}

package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.BannerDTO;
import com.lemnt.promotion.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Banner 详情执行器。
 */
@Component
@RequiredArgsConstructor
public class BannerDetailQueryExe {
    private final BannerManagementService service;

    public BannerDTO execute(Long id) {
        return service.detail(id);
    }
}

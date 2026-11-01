package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Banner 点击量累加执行器。 */
@Component
@RequiredArgsConstructor
public class BannerBrowseCmdExe {
    private final BannerManagementService service;

    public Boolean execute(Long id) {
        return service.addBrowseCount(id);
    }
}

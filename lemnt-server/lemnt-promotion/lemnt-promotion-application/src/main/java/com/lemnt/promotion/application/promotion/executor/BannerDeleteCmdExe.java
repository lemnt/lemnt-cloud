package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Banner 删除执行器。
 */
@Component
@RequiredArgsConstructor
public class BannerDeleteCmdExe {
    private final BannerManagementService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

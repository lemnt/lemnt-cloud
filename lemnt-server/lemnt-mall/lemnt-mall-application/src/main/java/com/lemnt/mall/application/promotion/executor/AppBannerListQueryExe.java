package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.AppBannerDTO;
import com.lemnt.mall.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序 Banner 列表执行器。 */
@Component
@RequiredArgsConstructor
public class AppBannerListQueryExe {
    private final BannerManagementService service;

    public List<AppBannerDTO> execute(Integer position) {
        return service.appList(position);
    }
}

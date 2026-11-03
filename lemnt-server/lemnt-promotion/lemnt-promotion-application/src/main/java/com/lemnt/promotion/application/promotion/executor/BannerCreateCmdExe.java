package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.BannerCreateCmd;
import com.lemnt.promotion.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Banner 新增执行器。 */
@Component
@RequiredArgsConstructor
public class BannerCreateCmdExe {
    private final BannerManagementService service;

    public Long execute(BannerCreateCmd cmd) {
        return service.create(cmd);
    }
}

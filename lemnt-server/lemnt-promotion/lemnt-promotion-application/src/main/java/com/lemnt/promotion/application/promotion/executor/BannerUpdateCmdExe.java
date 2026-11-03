package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.BannerUpdateCmd;
import com.lemnt.promotion.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Banner 修改执行器。 */
@Component
@RequiredArgsConstructor
public class BannerUpdateCmdExe {
    private final BannerManagementService service;

    public Boolean execute(BannerUpdateCmd cmd) {
        return service.update(cmd);
    }
}

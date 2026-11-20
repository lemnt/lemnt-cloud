package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiyPageSaveCmd;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 装修页面新增执行器。
 */
@Component
@RequiredArgsConstructor
public class DiyPageCreateCmdExe {
    private final DiyDecorationService service;

    public Long execute(DiyPageSaveCmd cmd) {
        return service.createPage(cmd);
    }
}

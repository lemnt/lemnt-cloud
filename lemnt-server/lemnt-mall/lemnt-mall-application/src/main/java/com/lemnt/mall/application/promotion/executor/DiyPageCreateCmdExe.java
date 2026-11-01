package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面新增执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPageCreateCmdExe {
    private final DiyDecorationService service;

    public Long execute(com.lemnt.mall.application.promotion.cmd.DiyPageSaveCmd cmd) {
        return service.createPage(cmd);
    }
}

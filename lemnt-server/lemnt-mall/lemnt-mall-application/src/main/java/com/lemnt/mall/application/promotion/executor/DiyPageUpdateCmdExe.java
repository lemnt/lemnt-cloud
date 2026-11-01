package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面修改执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPageUpdateCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(com.lemnt.mall.application.promotion.cmd.DiyPageSaveCmd cmd) {
        return service.updatePage(cmd);
    }
}

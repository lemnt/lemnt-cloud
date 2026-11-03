package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面修改执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPageUpdateCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(com.lemnt.promotion.application.promotion.cmd.DiyPageSaveCmd cmd) {
        return service.updatePage(cmd);
    }
}

package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面属性更新执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPagePropertyCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(DiyPropertyCmd cmd) {
        return service.updatePageProperty(cmd);
    }
}

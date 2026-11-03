package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.DiyPropertyCmd;
import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板属性更新执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplatePropertyCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(DiyPropertyCmd cmd) {
        return service.updateTemplateProperty(cmd);
    }
}

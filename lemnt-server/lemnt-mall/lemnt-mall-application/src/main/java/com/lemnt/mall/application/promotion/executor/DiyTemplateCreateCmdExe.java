package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板新增执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplateCreateCmdExe {
    private final DiyDecorationService service;

    public Long execute(com.lemnt.mall.application.promotion.cmd.DiyTemplateSaveCmd cmd) {
        return service.createTemplate(cmd);
    }
}

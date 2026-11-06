package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiyTemplateSaveCmd;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 装修模板新增执行器。
 */
@Component
@RequiredArgsConstructor
public class DiyTemplateCreateCmdExe {
    private final DiyDecorationService service;

    public Long execute(DiyTemplateSaveCmd cmd) {
        return service.createTemplate(cmd);
    }
}

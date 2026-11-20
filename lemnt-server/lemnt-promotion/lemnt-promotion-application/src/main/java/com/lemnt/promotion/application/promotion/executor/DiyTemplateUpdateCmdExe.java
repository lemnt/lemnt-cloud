package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.cmd.DiyTemplateSaveCmd;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 装修模板修改执行器。
 */
@Component
@RequiredArgsConstructor
public class DiyTemplateUpdateCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(DiyTemplateSaveCmd cmd) {
        return service.updateTemplate(cmd);
    }
}

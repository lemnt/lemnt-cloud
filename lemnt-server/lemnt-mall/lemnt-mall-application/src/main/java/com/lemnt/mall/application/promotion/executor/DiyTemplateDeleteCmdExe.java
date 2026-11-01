package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板删除执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplateDeleteCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(Long id) {
        return service.deleteTemplate(id);
    }
}

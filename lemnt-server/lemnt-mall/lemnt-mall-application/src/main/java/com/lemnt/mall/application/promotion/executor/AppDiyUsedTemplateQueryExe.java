package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序启用装修模板查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppDiyUsedTemplateQueryExe {
    private final DiyDecorationService service;

    public com.lemnt.mall.application.promotion.dto.AppDiyTemplateDTO execute() {
        return service.usedTemplate();
    }
}

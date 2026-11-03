package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.AppDiyTemplateDTO;
import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序指定装修模板查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppDiyTemplateQueryExe {
    private final DiyDecorationService service;

    public AppDiyTemplateDTO execute(Long id) {
        return service.appTemplate(id);
    }
}

package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板详情执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplateDetailQueryExe {
    private final DiyDecorationService service;

    public com.lemnt.promotion.application.promotion.dto.DiyTemplateDTO execute(Long id) {
        return service.templateDetail(id);
    }
}

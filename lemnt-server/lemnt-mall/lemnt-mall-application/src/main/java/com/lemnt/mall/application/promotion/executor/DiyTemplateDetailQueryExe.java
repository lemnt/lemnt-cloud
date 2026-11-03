package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.DiyTemplateDTO;
import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板详情执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplateDetailQueryExe {
    private final DiyDecorationService service;

    public DiyTemplateDTO execute(Long id) {
        return service.templateDetail(id);
    }
}

package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面详情执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPageDetailQueryExe {
    private final DiyDecorationService service;

    public com.lemnt.mall.application.promotion.dto.DiyPageDTO execute(Long id) {
        return service.pageDetail(id);
    }
}

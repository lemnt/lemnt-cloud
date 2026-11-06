package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.DiyPageDTO;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 装修页面详情执行器。
 */
@Component
@RequiredArgsConstructor
public class DiyPageDetailQueryExe {
    private final DiyDecorationService service;

    public DiyPageDTO execute(Long id) {
        return service.pageDetail(id);
    }
}

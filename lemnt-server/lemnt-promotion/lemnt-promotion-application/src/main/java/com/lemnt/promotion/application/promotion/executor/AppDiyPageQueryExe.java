package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.AppDiyPageDTO;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 小程序装修页面查询执行器。 */
@Component
@RequiredArgsConstructor
public class AppDiyPageQueryExe {
    private final DiyDecorationService service;

    public AppDiyPageDTO execute(Long id) {
        return service.appPage(id);
    }
}

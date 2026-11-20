package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 装修页面删除执行器。
 */
@Component
@RequiredArgsConstructor
public class DiyPageDeleteCmdExe {
    private final DiyDecorationService service;

    public Boolean execute(Long id) {
        return service.deletePage(id);
    }
}

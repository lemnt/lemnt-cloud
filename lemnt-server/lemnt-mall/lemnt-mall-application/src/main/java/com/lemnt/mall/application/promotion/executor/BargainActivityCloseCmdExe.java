package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动关闭执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityCloseCmdExe {
    private final BargainActivityManagementService service;

    public Boolean execute(Long id) {
        return service.close(id);
    }
}

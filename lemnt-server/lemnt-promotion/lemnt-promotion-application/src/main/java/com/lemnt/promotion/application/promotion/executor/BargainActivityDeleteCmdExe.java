package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价活动删除执行器。 */
@Component
@RequiredArgsConstructor
public class BargainActivityDeleteCmdExe {
    private final BargainActivityManagementService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

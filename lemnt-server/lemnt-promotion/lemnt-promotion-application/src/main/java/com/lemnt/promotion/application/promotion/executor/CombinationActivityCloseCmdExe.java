package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动关闭执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityCloseCmdExe {
    private final CombinationActivityManagementService service;

    public Boolean execute(Long id) {
        return service.close(id);
    }
}

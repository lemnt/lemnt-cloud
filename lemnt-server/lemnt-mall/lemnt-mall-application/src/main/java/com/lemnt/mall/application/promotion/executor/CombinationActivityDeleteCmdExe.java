package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团活动删除执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationActivityDeleteCmdExe {
    private final CombinationActivityManagementService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

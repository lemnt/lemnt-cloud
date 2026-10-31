package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.service.PointActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 积分商城活动关闭执行器。 */
@Component
@RequiredArgsConstructor
public class PointActivityCloseCmdExe {
    private final PointActivityManagementService service;

    public Boolean execute(Long id) {
        return service.close(id);
    }
}

package com.lemnt.order.application.pickup.executor;

import com.lemnt.order.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.order.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 自提门店新增执行器。
 */
@Component
@RequiredArgsConstructor
public class PickUpStoreCreateCmdExe {
    private final PickUpStoreService service;

    public Long execute(PickUpStoreSaveCmd cmd) {
        return service.create(cmd);
    }
}

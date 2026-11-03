package com.lemnt.order.application.pickup.executor;

import com.lemnt.order.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.order.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提门店修改执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpStoreUpdateCmdExe {
    private final PickUpStoreService service;

    public Boolean execute(PickUpStoreSaveCmd cmd) {
        return service.update(cmd);
    }
}

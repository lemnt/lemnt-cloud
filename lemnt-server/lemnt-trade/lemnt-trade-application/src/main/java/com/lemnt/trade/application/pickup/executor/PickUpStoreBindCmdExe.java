package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.cmd.PickUpBindCmd;
import com.lemnt.trade.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提门店店员绑定执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpStoreBindCmdExe {
    private final PickUpStoreService service;

    public Boolean execute(PickUpBindCmd cmd) {
        return service.bind(cmd);
    }
}

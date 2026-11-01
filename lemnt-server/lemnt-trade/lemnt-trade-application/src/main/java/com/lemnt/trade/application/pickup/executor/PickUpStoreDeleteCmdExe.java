package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提门店删除执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpStoreDeleteCmdExe {
    private final PickUpStoreService service;

    public Boolean execute(Long id) {
        return service.delete(id);
    }
}

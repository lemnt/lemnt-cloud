package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.trade.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提门店详情执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpStoreDetailQueryExe {
    private final PickUpStoreService service;

    public PickUpStoreDTO execute(Long id) {
        return service.detail(id);
    }
}

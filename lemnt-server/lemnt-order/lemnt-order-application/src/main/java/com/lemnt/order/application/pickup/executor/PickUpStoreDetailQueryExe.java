package com.lemnt.order.application.pickup.executor;

import com.lemnt.order.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.order.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 自提门店详情执行器。
 */
@Component
@RequiredArgsConstructor
public class PickUpStoreDetailQueryExe {
    private final PickUpStoreService service;

    public PickUpStoreDTO execute(Long id) {
        return service.detail(id);
    }
}

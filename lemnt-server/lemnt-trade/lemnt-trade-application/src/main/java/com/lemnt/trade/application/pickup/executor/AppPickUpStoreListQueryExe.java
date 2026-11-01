package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.dto.AppPickUpStoreDTO;
import com.lemnt.trade.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序自提门店列表执行器。 */
@Component
@RequiredArgsConstructor
public class AppPickUpStoreListQueryExe {
    private final PickUpStoreService service;

    public List<AppPickUpStoreDTO> execute() {
        return service.appList();
    }
}

package com.lemnt.order.application.pickup.executor;

import com.lemnt.order.application.pickup.dto.AppPickUpStoreDTO;
import com.lemnt.order.application.pickup.qry.AppPickUpStoreListQry;
import com.lemnt.order.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序自提门店列表执行器。 */
@Component
@RequiredArgsConstructor
public class AppPickUpStoreListQueryExe {
    private final PickUpStoreService service;

    public List<AppPickUpStoreDTO> execute(AppPickUpStoreListQry qry) {
        return service.appList(qry);
    }
}

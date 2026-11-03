package com.lemnt.order.application.pickup.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.order.application.pickup.qry.PickUpStorePageQry;
import com.lemnt.order.application.pickup.service.PickUpStoreService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提门店分页执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpStorePageQueryExe {
    private final PickUpStoreService service;

    public PageResult<PickUpStoreDTO> execute(PickUpStorePageQry qry) {
        return service.page(qry);
    }
}

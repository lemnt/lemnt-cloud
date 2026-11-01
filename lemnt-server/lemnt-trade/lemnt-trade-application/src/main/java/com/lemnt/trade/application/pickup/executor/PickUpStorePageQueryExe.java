package com.lemnt.trade.application.pickup.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.trade.application.pickup.qry.PickUpStorePageQry;
import com.lemnt.trade.application.pickup.service.PickUpStoreService;

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

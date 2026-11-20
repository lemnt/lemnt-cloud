package com.lemnt.order.domain.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.order.domain.delivery.model.PickUpStore;
import com.lemnt.order.domain.delivery.model.PickUpStoreFilter;

import java.util.List;

/**
 * 自提门店存储契约。
 */
public interface PickUpStoreGateway {
    PickUpStore find(Long id);

    PickUpStore save(PickUpStore value);

    void delete(Long id);

    PageResult<PickUpStore> page(PickUpStoreFilter filter);

    List<PickUpStore> enabled();
}

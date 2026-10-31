package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.CombinationFilter;

import java.util.List;

/** 拼团活动存储契约。 */
public interface CombinationActivityGateway {
    CombinationActivity find(Long id);

    CombinationActivity save(CombinationActivity value);

    boolean close(Long id);

    void delete(Long id);

    PageResult<CombinationActivity> page(CombinationFilter filter);

    List<CombinationActivity> enabled();

    List<CombinationActivity> byIds(List<Long> ids);

    List<CombinationActivity> enabledBySpu(Long spuId);
}

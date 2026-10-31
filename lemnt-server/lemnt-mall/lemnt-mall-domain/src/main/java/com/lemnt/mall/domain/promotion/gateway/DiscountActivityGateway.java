package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;
import com.lemnt.mall.domain.promotion.model.DiscountActivity;

import java.util.List;

/** DiscountActivity 活动主表访问。 */
public interface DiscountActivityGateway {
    DiscountActivity find(Long id);
    DiscountActivity save(DiscountActivity value);
    void delete(Long id);
    PageResult<DiscountActivity> page(ActivityFilter filter);
    List<DiscountActivity> enabled();
}

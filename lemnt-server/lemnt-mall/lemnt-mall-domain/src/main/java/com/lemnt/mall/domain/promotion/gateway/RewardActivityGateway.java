package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;
import com.lemnt.mall.domain.promotion.model.RewardActivity;

import java.util.List;

/** RewardActivity 活动主表访问。 */
public interface RewardActivityGateway {
    RewardActivity find(Long id);
    RewardActivity save(RewardActivity value);
    void delete(Long id);
    PageResult<RewardActivity> page(ActivityFilter filter);
    List<RewardActivity> enabled();
}

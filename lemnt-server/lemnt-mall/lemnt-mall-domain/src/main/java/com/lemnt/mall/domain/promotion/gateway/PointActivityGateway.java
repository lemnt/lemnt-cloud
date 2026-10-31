package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.PointActivity;
import com.lemnt.mall.domain.promotion.model.PointFilter;

import java.util.List;

/** 积分商城活动存储契约。 */
public interface PointActivityGateway {
    PointActivity find(Long id);

    PointActivity save(PointActivity value);

    boolean close(Long id);

    void delete(Long id);

    PageResult<PointActivity> page(PointFilter filter);

    List<PointActivity> enabled();

    /** 锁定活动行，串行扣减库存。 */
    PointActivity lockById(Long id);

    boolean decreaseStock(Long id, int count);

    boolean increaseStock(Long id, int count);
}

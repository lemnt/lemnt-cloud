package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.BargainFilter;

import java.util.List;

/** 砍价活动存储契约。 */
public interface BargainActivityGateway {
    BargainActivity find(Long id);

    BargainActivity save(BargainActivity value);

    boolean close(Long id);

    void delete(Long id);

    PageResult<BargainActivity> page(BargainFilter filter);

    List<BargainActivity> enabled();

    List<BargainActivity> all();

    /** 条件扣减库存，库存不足返回 false。 */
    boolean decreaseStock(Long id, int count);

    /** 恢复库存。 */
    boolean increaseStock(Long id, int count);
}

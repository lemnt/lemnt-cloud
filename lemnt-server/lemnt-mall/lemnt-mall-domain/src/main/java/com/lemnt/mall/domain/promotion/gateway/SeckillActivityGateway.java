package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.SeckillActivity;
import com.lemnt.mall.domain.promotion.model.SeckillFilter;

import java.util.List;

/** 秒杀活动存储契约。 */
public interface SeckillActivityGateway {
    SeckillActivity find(Long id);

    SeckillActivity save(SeckillActivity value);

    boolean close(Long id);

    void delete(Long id);

    PageResult<SeckillActivity> page(SeckillFilter filter);

    /** App 分页，只返回开启且活动时间覆盖当前时刻的活动。 */
    PageResult<SeckillActivity> appPage(Long configId, java.time.LocalDateTime now, int pageNum, int pageSize);

    /** 指定时段当前正在进行的活动。 */
    List<SeckillActivity> byConfigAndNow(Long configId, java.time.LocalDateTime now);

    List<SeckillActivity> enabled();

    List<SeckillActivity> all();

    List<SeckillActivity> byIds(List<Long> ids);

    List<SeckillActivity> enabledBySpuIds(List<Long> spuIds);

    /** 锁定活动行，串行扣减库存。 */
    SeckillActivity lockById(Long id);

    /** 原子扣减活动库存，库存不足时返回 false。 */
    boolean decreaseStock(Long id, int count);

    /** 恢复活动库存。 */
    boolean increaseStock(Long id, int count);
}

package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.SeckillConfig;
import com.lemnt.mall.domain.promotion.model.SeckillConfigFilter;

import java.util.List;

/** 秒杀时段存储契约。 */
public interface SeckillConfigGateway {
    SeckillConfig find(Long id);

    SeckillConfig save(SeckillConfig value);

    /** 条件更新状态，避免覆盖并发修改。 */
    boolean updateStatus(Long id, Integer status);

    void delete(Long id);

    List<SeckillConfig> all();

    List<SeckillConfig> byStatus(Integer status);

    List<SeckillConfig> byIds(List<Long> ids);

    PageResult<SeckillConfig> page(SeckillConfigFilter filter);

    /** 锁定时段行，串行校验时间区间冲突。 */
    SeckillConfig lockById(Long id);
}

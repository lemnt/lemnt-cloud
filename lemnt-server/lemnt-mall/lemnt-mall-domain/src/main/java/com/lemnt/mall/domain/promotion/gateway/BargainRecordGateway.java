package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.BargainRecord;
import com.lemnt.mall.domain.promotion.model.BargainRecordFilter;

import java.time.LocalDateTime;
import java.util.List;

/** 砍价记录存储契约。 */
public interface BargainRecordGateway {
    BargainRecord find(Long id);

    BargainRecord save(BargainRecord value);

    /** 按旧价格条件更新，保证并发助力只成功一次。 */
    boolean updatePrice(Long id, java.math.BigDecimal expected, java.math.BigDecimal price, Integer status,
            LocalDateTime endTime);

    boolean bindOrder(Long id, Long orderId);

    PageResult<BargainRecord> page(BargainRecordFilter filter);

    List<BargainRecord> byUserAndActivity(Long userId, Long activityId);

    long countByUserAndActivityAndStatus(Long userId, Long activityId, Integer status);

    List<BargainRecord> latest(Long activityId, Integer status, int count);

    long countByStatus(Integer status);

    List<BargainRecord> byActivityAndStatus(Long activityId, Integer status);

    /** 活动参与人数。 */
    long userCount(Long activityId, Integer status);
}

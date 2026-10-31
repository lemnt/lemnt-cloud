package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.promotion.model.CombinationRecord;
import com.lemnt.mall.domain.promotion.model.CombinationRecordFilter;

import java.time.LocalDateTime;
import java.util.List;

/** 拼团记录存储契约。 */
public interface CombinationRecordGateway {
    CombinationRecord find(Long id);

    CombinationRecord save(CombinationRecord value);

    /** 只更新状态、人数、结束时间和虚拟成团标记。 */
    boolean finish(Long id, Integer status, Integer userCount, Boolean virtualGroup, LocalDateTime endTime);

    /** 只更新订单号。 */
    boolean bindOrder(Long id, Long orderId);

    PageResult<CombinationRecord> page(CombinationRecordFilter filter);

    List<CombinationRecord> byHeadId(Long headId);

    List<CombinationRecord> byUserAndActivity(Long userId, Long activityId);

    /** 查询该团进行中的团长记录。 */
    CombinationRecord inProgressHead(Long headId);

    /** 查询过期的进行中团长记录。 */
    List<CombinationRecord> expiredHeads(LocalDateTime now);

    /** 最新参团头像。 */
    List<CombinationRecord> latest(int count);

    /** 参团用户总数。 */
    long userCount();
}

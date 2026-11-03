package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.PayNotifyTaskDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 通知任务CAS和租约数据访问，复杂更新统一在XML。 */
@Mapper
public interface PayNotifyTaskMapper extends BaseMapper<PayNotifyTaskDO> {
    int claim(
            @Param("id") Long id,
            @Param("expectedTimes") int expectedTimes,
            @Param("now") LocalDateTime now);

    int claimVersion(
            @Param("id") Long id,
            @Param("expectedTimes") int expectedTimes,
            @Param("version") long version,
            @Param("now") LocalDateTime now,
            @Param("until") LocalDateTime until);

    int finishSuccess(
            @Param("id") Long id,
            @Param("attemptNo") int attemptNo,
            @Param("response") String response,
            @Param("now") LocalDateTime now);

    int finishFailed(
            @Param("id") Long id,
            @Param("attemptNo") int attemptNo,
            @Param("response") String response,
            @Param("nextTime") LocalDateTime nextTime,
            @Param("now") LocalDateTime now);

    int finishVersion(
            @Param("id") Long id,
            @Param("version") long version,
            @Param("status") String status,
            @Param("response") String response,
            @Param("next") LocalDateTime next,
            @Param("now") LocalDateTime now);

    int resetForRetry(@Param("id") Long id, @Param("now") LocalDateTime now);

    List<PayNotifyTaskDO> findDue(@Param("now") LocalDateTime now, @Param("limit") int limit);

    LocalDateTime databaseNow();
}

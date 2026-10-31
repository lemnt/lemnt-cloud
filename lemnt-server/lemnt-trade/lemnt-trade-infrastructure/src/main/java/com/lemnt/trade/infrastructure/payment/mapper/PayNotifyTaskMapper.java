package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyTaskDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/** trade_pay_notify_task 数据访问接口；状态推进使用 CAS 更新。 */
@Mapper
public interface PayNotifyTaskMapper extends BaseMapper<PayNotifyTaskDO> {
    /** CAS 抢占一次尝试，只有次数与预期一致才成功。 */
    @Update("UPDATE trade_pay_notify_task SET notify_times=notify_times+1, last_execute_time=#{now},"
            + " update_time=#{now} WHERE id=#{id} AND status IN ('PENDING','FAILED')"
            + " AND notify_times=#{expectedTimes} AND notify_times < max_notify_times")
    int claim(@Param("id") Long id, @Param("expectedTimes") int expectedTimes, @Param("now") LocalDateTime now);

    /** 仅当前次数的尝试可标记成功。 */
    @Update("UPDATE trade_pay_notify_task SET status='SUCCESS', last_response=#{response},"
            + " next_notify_time=NULL, update_time=#{now} WHERE id=#{id} AND notify_times=#{attemptNo}")
    int finishSuccess(@Param("id") Long id, @Param("attemptNo") int attemptNo,
            @Param("response") String response, @Param("now") LocalDateTime now);

    /** 仅当前次数的尝试可标记失败并安排下次派发。 */
    @Update("UPDATE trade_pay_notify_task SET status='FAILED', last_response=#{response},"
            + " next_notify_time=#{nextTime}, update_time=#{now} WHERE id=#{id} AND notify_times=#{attemptNo}")
    int finishFailed(@Param("id") Long id, @Param("attemptNo") int attemptNo, @Param("response") String response,
            @Param("nextTime") LocalDateTime nextTime, @Param("now") LocalDateTime now);

    /** 人工重发：重置次数，成功任务不重置。 */
    @Update("UPDATE trade_pay_notify_task SET status='PENDING', notify_times=0, next_notify_time=#{now},"
            + " last_response=NULL, update_time=#{now} WHERE id=#{id} AND status<>'SUCCESS'")
    int resetForRetry(@Param("id") Long id, @Param("now") LocalDateTime now);

    /** 扫描到期可重试任务。 */
    @Select("SELECT * FROM trade_pay_notify_task WHERE status IN ('PENDING','FAILED')"
            + " AND notify_times < max_notify_times"
            + " AND (next_notify_time IS NULL OR next_notify_time <= #{now})"
            + " ORDER BY id LIMIT #{limit}")
    List<PayNotifyTaskDO> findDue(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /** 数据库当前时间。 */
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime databaseNow();
}

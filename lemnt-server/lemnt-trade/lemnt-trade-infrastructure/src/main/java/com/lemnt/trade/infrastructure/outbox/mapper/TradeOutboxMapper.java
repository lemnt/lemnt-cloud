package com.lemnt.trade.infrastructure.outbox.mapper;

import com.lemnt.trade.infrastructure.outbox.dataobject.TradeOutboxDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** TradeOutbox 表数据访问接口。 */
@Mapper
public interface TradeOutboxMapper extends BaseMapper<TradeOutboxDO> {
    String TradeOutbox_ELIGIBLE = "((status IN ('NEW','FAILED') AND next_attempt_at <= CURRENT_TIMESTAMP) "
            + "OR (status='PROCESSING' AND lease_until <= CURRENT_TIMESTAMP))";

    /**
     * 待处理。
     */
    @Select("SELECT * FROM trade_outbox WHERE retry_count < 10 AND " + TradeOutbox_ELIGIBLE
            + " ORDER BY id LIMIT #{limit}")
    List<TradeOutboxDO> pending(@Param("limit") int limit);

    /**
     * 耗尽额度。
     */
    @Update("UPDATE trade_outbox SET status='DEAD', lease_token=NULL, lease_until=NULL "
            + "WHERE retry_count >= 10 AND " + TradeOutbox_ELIGIBLE)
    int exhaust();

    /**
     * 领取。
     */
    @Update("UPDATE trade_outbox SET status='PROCESSING', lease_token=#{token}, lease_until=TIMESTAMPADD(SECOND, 300, CURRENT_TIMESTAMP), "
            + "retry_count=retry_count+1, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND retry_count < 10 AND " + TradeOutbox_ELIGIBLE)
    int claim(@Param("id") String id, @Param("token") String token);

    /**
     * 已投递。
     */
    @Update("UPDATE trade_outbox SET status='DELIVERED', lease_token=NULL, lease_until=NULL, "
            + "update_time=CURRENT_TIMESTAMP WHERE event_id=#{id} AND status='PROCESSING' "
            + "AND lease_token=#{token} AND lease_until > CURRENT_TIMESTAMP")
    int delivered(@Param("id") String id, @Param("token") String token);

    /**
     * 失败。
     */
    @Update("UPDATE trade_outbox SET status=CASE WHEN retry_count >= 10 THEN 'DEAD' ELSE 'FAILED' END, "
            + "next_attempt_at=TIMESTAMPADD(SECOND, #{delay}, CURRENT_TIMESTAMP), lease_token=NULL, lease_until=NULL, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND status='PROCESSING' AND lease_token=#{token} "
            + "AND lease_until > CURRENT_TIMESTAMP")
    int failed(@Param("id") String id, @Param("token") String token, @Param("delay") long delay);

    /**
     * 重放失效。
     */
    @Update("UPDATE trade_outbox SET status='NEW', retry_count=0, next_attempt_at=CURRENT_TIMESTAMP, "
            + "lease_token=NULL, lease_until=NULL, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND status='DEAD'")
    int replayDead(@Param("id") String id);
}

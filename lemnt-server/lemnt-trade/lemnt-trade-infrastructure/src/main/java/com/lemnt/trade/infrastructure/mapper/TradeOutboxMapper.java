package com.lemnt.trade.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.dataobject.TradeOutboxDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TradeOutboxMapper extends BaseMapper<TradeOutboxDO> {
    String ELIGIBLE = "((status IN ('NEW','FAILED') AND next_attempt_at <= CURRENT_TIMESTAMP) "
            + "OR (status='PROCESSING' AND lease_until <= CURRENT_TIMESTAMP))";

    @Select("SELECT * FROM trade_outbox WHERE retry_count < 10 AND " + ELIGIBLE
            + " ORDER BY id LIMIT #{limit}")
    List<TradeOutboxDO> pending(@Param("limit") int limit);

    @Update("UPDATE trade_outbox SET status='DEAD', lease_token=NULL, lease_until=NULL "
            + "WHERE retry_count >= 10 AND " + ELIGIBLE)
    int exhaust();

    @Update("UPDATE trade_outbox SET status='PROCESSING', lease_token=#{token}, lease_until=TIMESTAMPADD(SECOND, 300, CURRENT_TIMESTAMP), "
            + "retry_count=retry_count+1, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND retry_count < 10 AND " + ELIGIBLE)
    int claim(@Param("id") String id, @Param("token") String token);

    @Update("UPDATE trade_outbox SET status='DELIVERED', lease_token=NULL, lease_until=NULL, "
            + "update_time=CURRENT_TIMESTAMP WHERE event_id=#{id} AND status='PROCESSING' "
            + "AND lease_token=#{token} AND lease_until > CURRENT_TIMESTAMP")
    int delivered(@Param("id") String id, @Param("token") String token);

    @Update("UPDATE trade_outbox SET status=CASE WHEN retry_count >= 10 THEN 'DEAD' ELSE 'FAILED' END, "
            + "next_attempt_at=TIMESTAMPADD(SECOND, #{delay}, CURRENT_TIMESTAMP), lease_token=NULL, lease_until=NULL, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND status='PROCESSING' AND lease_token=#{token} "
            + "AND lease_until > CURRENT_TIMESTAMP")
    int failed(@Param("id") String id, @Param("token") String token, @Param("delay") long delay);

    @Update("UPDATE trade_outbox SET status='NEW', retry_count=0, next_attempt_at=CURRENT_TIMESTAMP, "
            + "lease_token=NULL, lease_until=NULL, update_time=CURRENT_TIMESTAMP "
            + "WHERE event_id=#{id} AND status='DEAD'")
    int replayDead(@Param("id") String id);
}

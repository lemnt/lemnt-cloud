package com.lemnt.trade.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.dataobject.TradeOutboxDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TradeOutboxMapper extends BaseMapper<TradeOutboxDO> {
    @Update("UPDATE trade_outbox SET status='PROCESSING', update_time=CURRENT_TIMESTAMP " +
            "WHERE event_id=#{id} AND (status='NEW' OR " +
            "(status='FAILED' AND update_time < DATE_SUB(NOW(), INTERVAL 1 MINUTE)) OR " +
            "(status='PROCESSING' AND update_time < DATE_SUB(NOW(), INTERVAL 5 MINUTE)))")
    int claim(@Param("id") String id);

    @Update("UPDATE trade_outbox SET status='DELIVERED', update_time=CURRENT_TIMESTAMP " +
            "WHERE event_id=#{id} AND status='PROCESSING'")
    int delivered(@Param("id") String id);

    @Update("UPDATE trade_outbox SET status='FAILED', retry_count=retry_count+1, update_time=CURRENT_TIMESTAMP " +
            "WHERE event_id=#{id} AND status='PROCESSING'")
    int failed(@Param("id") String id);
}

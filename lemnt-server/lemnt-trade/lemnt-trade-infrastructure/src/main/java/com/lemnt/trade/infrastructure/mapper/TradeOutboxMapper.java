package com.lemnt.trade.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.dataobject.TradeOutboxDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TradeOutboxMapper extends BaseMapper<TradeOutboxDO> {
    @Update("UPDATE trade_outbox SET status='PUBLISHED', update_time=CURRENT_TIMESTAMP " +
            "WHERE event_id=#{eventId} AND status='NEW'")
    int markPublished(@Param("eventId") String eventId);
}
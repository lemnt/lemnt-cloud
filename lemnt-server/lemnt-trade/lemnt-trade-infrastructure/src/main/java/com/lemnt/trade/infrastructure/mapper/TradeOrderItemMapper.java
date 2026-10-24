package com.lemnt.trade.infrastructure.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;import com.lemnt.trade.infrastructure.dataobject.TradeOrderItemDO;import org.apache.ibatis.annotations.Mapper;
@Mapper public interface TradeOrderItemMapper extends BaseMapper<TradeOrderItemDO> { }
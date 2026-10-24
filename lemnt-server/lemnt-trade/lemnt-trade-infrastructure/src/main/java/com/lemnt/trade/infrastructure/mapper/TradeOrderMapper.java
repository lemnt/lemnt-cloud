package com.lemnt.trade.infrastructure.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;import com.lemnt.trade.infrastructure.dataobject.TradeOrderDO;import org.apache.ibatis.annotations.Mapper;
@Mapper public interface TradeOrderMapper extends BaseMapper<TradeOrderDO> { }
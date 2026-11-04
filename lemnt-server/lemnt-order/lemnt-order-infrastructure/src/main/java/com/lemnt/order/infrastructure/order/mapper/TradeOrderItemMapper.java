package com.lemnt.order.infrastructure.order.mapper;

import com.lemnt.order.infrastructure.order.dataobject.TradeOrderItemDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * TradeOrderItem 表数据访问接口。
 */
@Mapper
public interface TradeOrderItemMapper extends BaseMapper<TradeOrderItemDO> {
}

package com.lemnt.trade.infrastructure.delivery.mapper;

import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * trade_delivery_express Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface DeliveryExpressMapper extends BaseMapper<DeliveryExpressDO> {
}

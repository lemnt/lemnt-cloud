package com.lemnt.order.infrastructure.delivery.mapper;

import com.lemnt.order.infrastructure.delivery.dataobject.DeliveryExpressDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * order_delivery_express Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface DeliveryExpressMapper extends BaseMapper<DeliveryExpressDO> {
}

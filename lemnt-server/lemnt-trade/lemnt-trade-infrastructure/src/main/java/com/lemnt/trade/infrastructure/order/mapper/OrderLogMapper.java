package com.lemnt.trade.infrastructure.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.trade.infrastructure.order.dataobject.OrderLogDO;
import org.apache.ibatis.annotations.Mapper;

/** 订单日志复用 MyBatis-Plus 标准CRUD。 */
@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLogDO> {
}

package com.lemnt.order.infrastructure.order.mapper;

import com.lemnt.order.infrastructure.order.dataobject.OrderLogDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单日志复用 MyBatis-Plus 标准CRUD。
 */
@Mapper
public interface OrderLogMapper extends BaseMapper<OrderLogDO> {
}

package com.lemnt.order.infrastructure.brokerage.mapper;

import com.lemnt.order.infrastructure.brokerage.dataobject.OrderCommissionLineDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderCommissionLineMapper extends BaseMapper<OrderCommissionLineDO> {}

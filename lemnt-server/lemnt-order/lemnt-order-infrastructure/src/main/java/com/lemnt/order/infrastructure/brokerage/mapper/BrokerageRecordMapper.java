package com.lemnt.order.infrastructure.brokerage.mapper;

import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * BrokerageRecord 表 CRUD。
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapper<BrokerageRecordDO> {
}

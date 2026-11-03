package com.lemnt.trade.infrastructure.brokerage.mapper;

import com.lemnt.trade.infrastructure.brokerage.dataobject.BrokerageUserDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** BrokerageUser 表 CRUD。 */
@Mapper
public interface BrokerageUserMapper extends BaseMapper<BrokerageUserDO> {}

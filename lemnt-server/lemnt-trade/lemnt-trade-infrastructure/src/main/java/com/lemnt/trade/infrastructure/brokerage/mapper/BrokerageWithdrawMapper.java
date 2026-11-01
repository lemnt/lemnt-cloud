package com.lemnt.trade.infrastructure.brokerage.mapper;

import com.lemnt.trade.infrastructure.brokerage.dataobject.BrokerageWithdrawDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** BrokerageWithdraw 表 CRUD。 */
@Mapper
public interface BrokerageWithdrawMapper extends BaseMapper<BrokerageWithdrawDO> {
}

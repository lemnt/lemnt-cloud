package com.lemnt.order.infrastructure.brokerage.mapper;

import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageWithdrawDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** BrokerageWithdraw 表 CRUD。 */
@Mapper
public interface BrokerageWithdrawMapper extends BaseMapper<BrokerageWithdrawDO> {
    BrokerageWithdrawDO lock(@Param("id") Long id);
}

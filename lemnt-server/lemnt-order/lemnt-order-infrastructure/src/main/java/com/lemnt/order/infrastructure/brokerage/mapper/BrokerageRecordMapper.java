package com.lemnt.order.infrastructure.brokerage.mapper;

import com.lemnt.order.infrastructure.brokerage.dataobject.BrokerageRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * BrokerageRecord 表 CRUD。
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapper<BrokerageRecordDO> {
    /** 锁定记录，避免同时解冻和按旧状态扣回。 */
    BrokerageRecordDO lock(@Param("id") Long id);
}

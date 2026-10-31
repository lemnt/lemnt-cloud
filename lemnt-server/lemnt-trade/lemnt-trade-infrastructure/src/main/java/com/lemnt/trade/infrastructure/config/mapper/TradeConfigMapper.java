package com.lemnt.trade.infrastructure.config.mapper;

import com.lemnt.trade.infrastructure.config.dataobject.TradeConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** trade_config Mapper，CRUD 复用 MyBatis Plus。 */
@Mapper
public interface TradeConfigMapper extends BaseMapper<TradeConfigDO> {
    /** 锁定主记录，供业务事务串行维护。 */
    TradeConfigDO lockById(@Param("id") Long id);
}

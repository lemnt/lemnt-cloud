package com.lemnt.trade.infrastructure.aftersale.mapper;

import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleLogDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/** 售后日志复用 MyBatis-Plus 标准CRUD。 */
@Mapper
public interface AfterSaleLogMapper extends BaseMapper<AfterSaleLogDO> {
}

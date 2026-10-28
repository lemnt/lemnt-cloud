package com.lemnt.trade.infrastructure.aftersale.mapper;

import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 简单CRUD复用MyBatis-Plus，仅行锁查询使用XML。 */
@Mapper
public interface AfterSaleMapper extends BaseMapper<AfterSaleDO> {
    /** 锁定业务单号对应的售后。 */
    AfterSaleDO lockByNo(@Param("no") String no);
}

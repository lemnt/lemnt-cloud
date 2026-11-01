package com.lemnt.trade.infrastructure.delivery.mapper;

import com.lemnt.trade.infrastructure.delivery.dataobject.PickUpStoreDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 自提门店表 CRUD。 */
@Mapper
public interface PickUpStoreMapper extends BaseMapper<PickUpStoreDO> {
}

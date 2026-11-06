package com.lemnt.order.infrastructure.delivery.mapper;

import com.lemnt.order.infrastructure.delivery.dataobject.PickUpStoreDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自提门店表 CRUD。
 */
@Mapper
public interface PickUpStoreMapper extends BaseMapper<PickUpStoreDO> {
}

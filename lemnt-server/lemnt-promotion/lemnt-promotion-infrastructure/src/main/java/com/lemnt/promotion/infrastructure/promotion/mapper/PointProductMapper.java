package com.lemnt.promotion.infrastructure.promotion.mapper;

import com.lemnt.promotion.infrastructure.promotion.dataobject.PointProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分商城活动商品表 CRUD。
 */
@Mapper
public interface PointProductMapper extends BaseMapper<PointProductDO> {
}

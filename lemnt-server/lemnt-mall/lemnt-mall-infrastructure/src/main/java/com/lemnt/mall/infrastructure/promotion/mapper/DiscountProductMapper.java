package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.DiscountProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 复用 MyBatis Plus 的活动表 CRUD。 */
@Mapper
public interface DiscountProductMapper extends BaseMapper<DiscountProductDO> {
}

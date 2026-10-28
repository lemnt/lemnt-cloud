package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.RewardActivityDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 复用 MyBatis Plus 的活动表 CRUD。 */
@Mapper
public interface RewardActivityMapper extends BaseMapper<RewardActivityDO> {
}

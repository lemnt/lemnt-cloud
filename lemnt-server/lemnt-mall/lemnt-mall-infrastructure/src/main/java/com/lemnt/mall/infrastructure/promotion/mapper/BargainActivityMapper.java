package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.BargainActivityDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 砍价活动表 CRUD。 */
@Mapper
public interface BargainActivityMapper extends BaseMapper<BargainActivityDO> {
}

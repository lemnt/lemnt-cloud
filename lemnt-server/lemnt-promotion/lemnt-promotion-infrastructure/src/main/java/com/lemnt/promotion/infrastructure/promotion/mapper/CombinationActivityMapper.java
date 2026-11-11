package com.lemnt.promotion.infrastructure.promotion.mapper;

import com.lemnt.promotion.infrastructure.promotion.dataobject.CombinationActivityDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 拼团活动表 CRUD。
 */
@Mapper
public interface CombinationActivityMapper extends BaseMapper<CombinationActivityDO> {
}

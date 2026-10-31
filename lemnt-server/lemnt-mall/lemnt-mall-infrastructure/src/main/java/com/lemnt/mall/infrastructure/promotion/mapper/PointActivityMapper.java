package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PointActivityDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 积分商城活动表 CRUD 及库存扣减行锁。 */
@Mapper
public interface PointActivityMapper extends BaseMapper<PointActivityDO> {

    /** 锁定活动行。 */
    PointActivityDO lockById(Long id);
}

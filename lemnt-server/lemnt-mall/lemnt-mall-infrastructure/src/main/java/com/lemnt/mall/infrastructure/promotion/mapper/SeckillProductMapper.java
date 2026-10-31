package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 秒杀活动商品表 CRUD。 */
@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProductDO> {
}

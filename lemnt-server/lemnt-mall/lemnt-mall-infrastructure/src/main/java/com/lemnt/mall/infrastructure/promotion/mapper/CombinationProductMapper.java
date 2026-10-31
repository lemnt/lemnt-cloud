package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationProductDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 拼团活动商品表 CRUD。 */
@Mapper
public interface CombinationProductMapper extends BaseMapper<CombinationProductDO> {
}

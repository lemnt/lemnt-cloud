package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderItemDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 活动订单商品行表 CRUD。 */
@Mapper
public interface PromotionOrderItemMapper extends BaseMapper<PromotionOrderItemDO> {
}

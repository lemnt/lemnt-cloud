package com.lemnt.order.infrastructure.promotion.mapper;

import com.lemnt.order.infrastructure.promotion.dataobject.OrderItemPromotionDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 优惠在原订单商品明细上的分摊数据访问。 */
@Mapper
public interface OrderItemPromotionMapper extends BaseMapper<OrderItemPromotionDO> {}

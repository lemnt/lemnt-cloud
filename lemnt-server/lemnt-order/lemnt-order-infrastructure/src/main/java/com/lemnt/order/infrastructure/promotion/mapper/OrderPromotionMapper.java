package com.lemnt.order.infrastructure.promotion.mapper;

import com.lemnt.order.infrastructure.promotion.dataobject.OrderPromotionDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单实际采用的优惠与权益结果数据访问。
 */
@Mapper
public interface OrderPromotionMapper extends BaseMapper<OrderPromotionDO> {
}

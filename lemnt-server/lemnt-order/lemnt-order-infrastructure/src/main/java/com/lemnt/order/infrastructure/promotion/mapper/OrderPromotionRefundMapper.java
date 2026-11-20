package com.lemnt.order.infrastructure.promotion.mapper;

import com.lemnt.order.infrastructure.promotion.dataobject.OrderPromotionRefundDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生成售后时冻结的优惠回退归因数据访问。
 */
@Mapper
public interface OrderPromotionRefundMapper extends BaseMapper<OrderPromotionRefundDO> {
}

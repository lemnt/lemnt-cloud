package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderRefundDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 活动订单退款去重表 CRUD。 */
@Mapper
public interface PromotionOrderRefundMapper extends BaseMapper<PromotionOrderRefundDO> {
}

package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 活动订单映射表 CRUD 及生命周期行锁。 */
@Mapper
public interface PromotionOrderMapper extends BaseMapper<PromotionOrderDO> {

    /** 锁定映射行。 */
    PromotionOrderDO lockByOrderNo(String orderNo);
}

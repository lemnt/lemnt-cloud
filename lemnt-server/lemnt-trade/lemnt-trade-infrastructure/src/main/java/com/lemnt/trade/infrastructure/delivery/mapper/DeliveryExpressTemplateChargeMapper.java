package com.lemnt.trade.infrastructure.delivery.mapper;

import com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * trade_delivery_express_template_charge Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface DeliveryExpressTemplateChargeMapper extends BaseMapper<DeliveryExpressTemplateChargeDO> {
}

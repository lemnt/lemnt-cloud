package com.lemnt.order.infrastructure.delivery.mapper;

import com.lemnt.order.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * order_delivery_express_template_charge Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface DeliveryExpressTemplateChargeMapper
    extends BaseMapper<DeliveryExpressTemplateChargeDO> {
}

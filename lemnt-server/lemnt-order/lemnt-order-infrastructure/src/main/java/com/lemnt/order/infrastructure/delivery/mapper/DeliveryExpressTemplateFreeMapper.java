package com.lemnt.order.infrastructure.delivery.mapper;

import com.lemnt.order.infrastructure.delivery.dataobject.DeliveryExpressTemplateFreeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * order_delivery_express_template_free Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface DeliveryExpressTemplateFreeMapper
    extends BaseMapper<DeliveryExpressTemplateFreeDO> {
}

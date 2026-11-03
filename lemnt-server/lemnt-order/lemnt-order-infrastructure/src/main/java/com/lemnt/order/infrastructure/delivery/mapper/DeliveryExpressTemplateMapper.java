package com.lemnt.order.infrastructure.delivery.mapper;

import com.lemnt.order.infrastructure.delivery.dataobject.DeliveryExpressTemplateDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** order_delivery_express_template Mapper，CRUD 复用 MyBatis Plus。 */
@Mapper
public interface DeliveryExpressTemplateMapper extends BaseMapper<DeliveryExpressTemplateDO> {
    /** 锁定主记录，供业务事务串行维护。 */
    DeliveryExpressTemplateDO lockById(@Param("id") Long id);
}

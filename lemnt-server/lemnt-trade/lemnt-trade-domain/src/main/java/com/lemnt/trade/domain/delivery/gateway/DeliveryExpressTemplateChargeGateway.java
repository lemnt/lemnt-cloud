package com.lemnt.trade.domain.delivery.gateway;

import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateCharge;

import java.util.List;

/** trade_delivery_express_template_charge 领域存储接口。 */
public interface DeliveryExpressTemplateChargeGateway {
    /** 读取模板子项。 */
    List<DeliveryExpressTemplateCharge> byTemplate(Long templateId);

    /** 新增记录。 */
    DeliveryExpressTemplateCharge insert(DeliveryExpressTemplateCharge value);

    /** 修改记录。 */
    boolean update(DeliveryExpressTemplateCharge value);

    /** 移除指定子项。 */
    void deleteIds(List<Long> ids);

    /** 移除模板全部子项。 */
    void deleteByTemplate(Long templateId);

}

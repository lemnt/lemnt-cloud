package com.lemnt.order.domain.delivery.gateway;

import com.lemnt.order.domain.delivery.model.DeliveryExpressTemplateFree;

import java.util.List;

/** order_delivery_express_template_free 领域存储接口。 */
public interface DeliveryExpressTemplateFreeGateway {
    /** 读取模板子项。 */
    List<DeliveryExpressTemplateFree> byTemplate(Long templateId);

    /** 新增记录。 */
    DeliveryExpressTemplateFree insert(DeliveryExpressTemplateFree value);

    /** 修改记录。 */
    boolean update(DeliveryExpressTemplateFree value);

    /** 移除指定子项。 */
    void deleteIds(List<Long> ids);

    /** 移除模板全部子项。 */
    void deleteByTemplate(Long templateId);
}

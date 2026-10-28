package com.lemnt.trade.domain.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplate;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFilter;

import java.util.List;

/** trade_delivery_express_template 领域存储接口。 */
public interface DeliveryExpressTemplateGateway {
    /** 读取单条。 */
    DeliveryExpressTemplate find(Long id);

    /** 锁定记录。 */
    DeliveryExpressTemplate lock(Long id);

    /** 查询管理列表。 */
    List<DeliveryExpressTemplate> list(String name);

    /** 新增记录。 */
    DeliveryExpressTemplate insert(DeliveryExpressTemplate value);

    /** 修改记录。 */
    boolean update(DeliveryExpressTemplate value);

    /** 逻辑删除。 */
    boolean delete(Long id);
    /** 分页查询。 */
    PageResult<DeliveryExpressTemplate> page(DeliveryExpressTemplateFilter filter);

}

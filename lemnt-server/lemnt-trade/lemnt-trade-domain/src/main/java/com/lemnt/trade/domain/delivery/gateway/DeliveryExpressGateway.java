package com.lemnt.trade.domain.delivery.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.delivery.model.DeliveryExpress;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressFilter;

import java.util.List;

/** trade_delivery_express 领域存储接口。 */
public interface DeliveryExpressGateway {
    /** 读取单条。 */
    DeliveryExpress find(Long id);

    /** 按编码读取。 */
    DeliveryExpress byCode(String code);

    /** 查询管理列表。 */
    List<DeliveryExpress> list(String name, Integer status);

    /** 新增记录。 */
    DeliveryExpress insert(DeliveryExpress value);

    /** 修改记录。 */
    boolean update(DeliveryExpress value);

    /** 逻辑删除。 */
    boolean delete(Long id);
    /** 分页查询。 */
    PageResult<DeliveryExpress> page(DeliveryExpressFilter filter);

}

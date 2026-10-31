package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.domain.payment.model.PayAppFilter;

/** 支付应用表存储契约。 */
public interface PayAppGateway {
    /** 分页查询支付应用。 */
    PageResult<PayApp> page(PayAppFilter filter);

    /** 按主键查询。 */
    PayApp find(Long id);

    /** 按应用标识查询。 */
    PayApp findByAppKey(String appKey);

    /** 应用标识是否已被其他应用占用。 */
    boolean existsAppKey(String appKey, Long excludeId);

    /** 新增应用，由数据库分配主键。 */
    PayApp insert(PayApp value);

    /** 全量更新允许修改的字段。 */
    boolean update(PayApp value);

    /** 逻辑删除。 */
    boolean delete(Long id);
}

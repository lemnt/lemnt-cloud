package com.lemnt.mall.domain.checkout.gateway;

import com.lemnt.mall.domain.checkout.model.Settlement;

/**
 * 结算领域网关。
 */
public interface SettlementGateway {
    /**
     * 缓存。
     */
    void cache(Settlement value);

    /** 保存已确认的提交快照，使 RPC 超时后可按原内容重试。 */
    void cacheOrder(com.lemnt.mall.domain.order.model.OrderSnapshot value);

    /** 读取原提交快照。 */
    com.lemnt.mall.domain.order.model.OrderSnapshot cachedOrder(String orderNo);

    /**
     * 已缓存。
     */
    Settlement cached(String id);
}

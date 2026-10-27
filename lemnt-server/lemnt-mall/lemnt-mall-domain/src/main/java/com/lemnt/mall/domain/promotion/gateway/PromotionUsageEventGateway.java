package com.lemnt.mall.domain.promotion.gateway;

/** PromotionUsageEvent 表领域存储接口。 */
public interface PromotionUsageEventGateway {
    /** 追加促销占用状态事件。 */
    void append(String orderNo, long rootId, String from, String to, String reason, long now);
}

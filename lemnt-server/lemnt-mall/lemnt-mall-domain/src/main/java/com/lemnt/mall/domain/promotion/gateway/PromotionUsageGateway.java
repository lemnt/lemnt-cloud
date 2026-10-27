package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionUsage;

/** PromotionUsage 表领域存储接口。 */
public interface PromotionUsageGateway {
    /**
     * 锁定使用。
     */
    PromotionUsage lockUsage(String order, long id);

    /** 保存促销占用记录。 */
    void insertUsage(PromotionUsage value, long now);

    /** 完成促销占用记录。 */
    void finishUsage(PromotionUsage value, PromotionUsage.State state, long now);
}

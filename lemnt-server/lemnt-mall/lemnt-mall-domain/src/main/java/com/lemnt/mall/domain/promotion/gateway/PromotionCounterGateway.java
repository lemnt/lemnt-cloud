package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionCounter;

/** PromotionCounter 表领域存储接口。 */
public interface PromotionCounterGateway {
    /**
     * 锁定计数器。
     */
    PromotionCounter lockCounter(long id);

    /**
     * 设置限制。
     */
    void setLimits(long id, Long global, Long member);

    /**
     * 获取全局预算。
     */
    boolean takeGlobal(long id);

    /** 初始化促销额度计数器。 */
    void initialize(long id);

    /** 结束占用的全局额度。 */
    void finish(long id, String state);
}

package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PublishedPromotion;

/** PromotionScope 表领域存储接口。 */
public interface PromotionScopeGateway {
    /** 写入促销版本的范围集合。 */
    void saveScope(PublishedPromotion value);
}

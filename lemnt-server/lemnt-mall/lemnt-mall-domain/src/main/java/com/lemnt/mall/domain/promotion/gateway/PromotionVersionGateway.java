package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PublishedPromotion;

/** PromotionVersion 表领域存储接口。 */
public interface PromotionVersionGateway {
    /**
     * 版本。
     */
    PublishedPromotion version(long root, int version);

    /** 写入促销版本主体，范围由应用层编排。 */
    void insertVersion(PublishedPromotion v);
}

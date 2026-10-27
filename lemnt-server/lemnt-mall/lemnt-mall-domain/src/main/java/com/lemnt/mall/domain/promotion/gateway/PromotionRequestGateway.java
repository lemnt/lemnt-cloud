package com.lemnt.mall.domain.promotion.gateway;

import com.lemnt.mall.domain.promotion.model.PromotionReceipt;
import com.lemnt.mall.domain.promotion.model.PromotionRequest;

/** PromotionRequest 表领域存储接口。 */
public interface PromotionRequestGateway {
    /**
     * 锁定后台请求。
     */
    PromotionRequest lockRequest(
            long operator, String request, String action, String fingerprint);

    /**
     * 完成后台配置请求。
     */
    void completeRequest(long operator, String request, PromotionReceipt receipt);
}

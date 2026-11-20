package com.lemnt.promotion.client;

import com.lemnt.promotion.client.bo.PromotionLockBo;
import com.lemnt.promotion.client.dto.PromotionLockDTO;

/**
 * 同步冻结原券/活动资源，支付确认与释放也由本库幂等凭证保护。
 */
public interface PromotionLockClient {
    boolean refundApplied(String afterSaleNo);

    PromotionLockDTO lock(PromotionLockBo input);

    PromotionLockDTO confirm(String orderNo);

    void rollback(String orderNo);

    PromotionLockDTO query(String orderNo);
}

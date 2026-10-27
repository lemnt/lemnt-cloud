package com.lemnt.mall.domain.store.gateway;

import java.util.List;

/** CouponPublicClaim 表领域存储接口。 */
public interface CouponPublicClaimGateway {
    /**
     * 可领取。
     */
    boolean claimable(Long id);

    /**
     * 可领取。
     */
    void claimable(Long id, boolean enabled);

    /**
     * 公开模板编号集合。
     */
    List<Long> publicTemplateIds();
}

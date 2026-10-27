package com.lemnt.mall.adapter.web.store.converter;

import com.lemnt.mall.adapter.web.store.req.CouponPublicClaimReq;
import com.lemnt.mall.application.store.cmd.CouponPublicClaimCmd;

/**
 * 营销请求转换器。
 */
public final class PromotionReqConverter {
    private PromotionReqConverter() {}

    /**
     * 转换公开领取配置命令。
     */
    public static CouponPublicClaimCmd setting(Long t, CouponPublicClaimReq r) {
        return new CouponPublicClaimCmd(t, r.enabled());
    }
}

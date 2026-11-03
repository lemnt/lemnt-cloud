package com.lemnt.promotion.application.dubbo;

import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.client.PromotionLockClient;
import com.lemnt.promotion.client.bo.PromotionLockBo;
import com.lemnt.promotion.client.dto.PromotionLockDTO;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/** RPC只转交本地幂等资源编排，不跨库写订单。 */
@DubboService
@RequiredArgsConstructor
public class PromotionLockClientImpl implements PromotionLockClient {
    private final PromotionResourceService service;

    public boolean refundApplied(String no) {
        return service.refundApplied(no);
    }

    public PromotionLockDTO lock(PromotionLockBo value) {
        return service.lock(value);
    }

    public PromotionLockDTO confirm(String no) {
        return service.confirm(no);
    }

    public void rollback(String no) {
        service.rollback(no);
    }

    public PromotionLockDTO query(String no) {
        return service.query(no);
    }
}

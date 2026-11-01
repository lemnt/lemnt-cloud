package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.coupon.service.CouponIssuanceService;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/** 订单支付成功后按订单快照发放满减活动赠券，重复回调不重复发放。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewardGiftService {
    private final CouponGateway coupons;
    private final CouponIssuanceService issuance;

    /** 每个模板按数量逐张发放；模板下架或名额用尽只记录告警，不阻断支付回调。 */
    public void issuePaid(SettlementOrder order) {
        Map<Long, Integer> gifts = order.giveCouponTemplateCounts();
        if (gifts == null || gifts.isEmpty()) {
            return;
        }
        for (var entry : gifts.entrySet()) {
            long issued = coupons.giftCount(order.orderNo(), entry.getKey(), order.memberId());
            for (long index = issued; index < entry.getValue(); index++) {
                try {
                    issuance.gift(order.memberId(), entry.getKey(), order.orderNo());
                } catch (ServiceException exception) {
                    log.warn("订单赠券未发放, orderNo={}, templateId={}, reason={}",
                            order.orderNo(), entry.getKey(), exception.getMessage());
                    break;
                }
            }
        }
    }
}

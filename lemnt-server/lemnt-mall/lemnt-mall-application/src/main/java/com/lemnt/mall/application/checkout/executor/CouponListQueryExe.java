package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 优惠券列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponListQueryExe {
    private final CouponGateway couponGateway;

    /**
     * 执行优惠券列表查询。
     */
    public List<CouponDTO> execute(Long memberId) {
        CheckoutRules.id(memberId);
        return couponGateway.coupons(memberId).stream().map(CheckoutDTOConverter::coupon).toList();
    }
}

package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.application.coupon.converter.CouponDTOConverter;
import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.model.CouponRules;

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
        CouponRules.id(memberId);
        return couponGateway.coupons(memberId).stream().map(CouponDTOConverter::coupon).toList();
    }
}

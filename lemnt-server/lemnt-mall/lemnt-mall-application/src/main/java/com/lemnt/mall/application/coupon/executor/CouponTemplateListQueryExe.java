package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.application.coupon.converter.CouponDTOConverter;
import com.lemnt.mall.application.coupon.dto.CouponTemplateDTO;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 优惠券模板列表查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponTemplateListQueryExe {
    private final CouponTemplateGateway couponTemplateGateway;

    /**
     * 执行优惠券模板列表查询。
     */
    public List<CouponTemplateDTO> execute() {
        return couponTemplateGateway.templates().stream().map(CouponDTOConverter::template).toList();
    }
}

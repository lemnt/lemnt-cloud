package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;

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
        return couponTemplateGateway.templates().stream().map(CheckoutDTOConverter::template).toList();
    }
}

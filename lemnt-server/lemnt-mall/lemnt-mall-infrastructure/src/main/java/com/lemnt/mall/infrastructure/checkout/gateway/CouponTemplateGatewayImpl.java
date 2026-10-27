package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.infrastructure.checkout.converter.CheckoutConverter;
import com.lemnt.mall.infrastructure.checkout.dataobject.CouponTemplateDO;
import com.lemnt.mall.infrastructure.checkout.mapper.CouponTemplateMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** CouponTemplate 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CouponTemplateGatewayImpl implements CouponTemplateGateway {
    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public List<CouponTemplate> templates() {
        var values = couponTemplateMapper.templates();
        if (values.size() > 1000) {
            throw new IllegalStateException("优惠券模板超过1000条，请接入分页");
        }
        return values.stream().map(CheckoutConverter::template).toList();
    }

    @Override
    public CouponTemplate template(Long id) {
        return CheckoutConverter.template(couponTemplateMapper.template(id));
    }

    @Override
    public CouponTemplate lockTemplate(Long id) {
        return CheckoutConverter.template(couponTemplateMapper.lockTemplate(id));
    }

    @Override
    public CouponTemplate createTemplate(CouponTemplate value) {
        var row = new CouponTemplateDO();
        row.setCode(value.code());
        row.setName(value.name());
        row.setKind(value.kind());
        row.setThreshold(value.threshold());
        row.setFixedAmount(value.fixedAmount());
        row.setDiscountPercent(value.discountPercent());
        row.setDiscountLimit(value.discountLimit());
        row.setTotalCount(value.totalCount());
        row.setIssuedCount(0);
        row.setValidFrom(value.validFrom());
        row.setValidUntil(value.validUntil());
        row.setEnabled(true);
        couponTemplateMapper.insertTemplate(row);
        return CheckoutConverter.template(couponTemplateMapper.template(row.getId()));
    }

    @Override
    public boolean enableTemplate(Long id, boolean enabled) {
        return couponTemplateMapper.enableTemplate(id, enabled) == 1;
    }

    @Override
    /**
     * 可领取优惠。
     */
    public List<CouponTemplate> offers(int offset, int limit) {
        return couponTemplateMapper.offers(offset, limit).stream().map(CheckoutConverter::template).toList();
    }

    @Override
    /**
     * 可领取优惠数量。
     */
    public long offerCount() {
        return couponTemplateMapper.offerCount();
    }

    /** 占用优惠券发放名额。 */
    @Override
    public boolean takeQuota(Long templateId) {
        return couponTemplateMapper.takeQuota(templateId) == 1;
    }
}

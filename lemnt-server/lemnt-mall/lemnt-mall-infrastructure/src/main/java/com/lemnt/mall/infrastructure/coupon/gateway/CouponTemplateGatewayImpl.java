package com.lemnt.mall.infrastructure.coupon.gateway;

import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.infrastructure.coupon.converter.CouponConverter;
import com.lemnt.mall.infrastructure.coupon.dataobject.CouponTemplateDO;
import com.lemnt.mall.infrastructure.coupon.mapper.CouponTemplateMapper;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** CouponTemplate 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CouponTemplateGatewayImpl implements CouponTemplateGateway {
    private final CouponTemplateMapper couponTemplateMapper;

    @Override
    public List<CouponTemplate> templates() {
        var values = couponTemplateMapper.selectList(Wrappers.<CouponTemplateDO>lambdaQuery()
                .orderByDesc(CouponTemplateDO::getId)
                .last("LIMIT 1001"));
        if (values.size() > 1000) {
            throw new IllegalStateException("优惠券模板超过1000条，请接入分页");
        }
        return values.stream().map(CouponConverter::template).toList();
    }

    @Override
    public CouponTemplate template(Long id) {
        return CouponConverter.template(couponTemplateMapper.selectById(id));
    }

    @Override
    public CouponTemplate lockTemplate(Long id) {
        return CouponConverter.template(couponTemplateMapper.lockTemplate(id));
    }

    @Override
    public CouponTemplate createTemplate(CouponTemplate value) {
        var row = CouponConverter.template(value);
        couponTemplateMapper.insert(row);
        return CouponConverter.template(couponTemplateMapper.selectById(row.getId()));
    }

    /** 更新模板不触碰会员券中已经保存的规则。 */
    @Override
    public boolean updateTemplate(CouponTemplate value) {
        return couponTemplateMapper.update(null, Wrappers.<CouponTemplateDO>lambdaUpdate()
                .eq(CouponTemplateDO::getId, value.id())
                .set(CouponTemplateDO::getCode, value.code())
                .set(CouponTemplateDO::getName, value.name())
                .set(CouponTemplateDO::getKind, value.kind())
                .set(CouponTemplateDO::getThreshold, value.threshold())
                .set(CouponTemplateDO::getFixedAmount, value.fixedAmount())
                .set(CouponTemplateDO::getDiscountPercent, value.discountPercent())
                .set(CouponTemplateDO::getDiscountLimit, value.discountLimit())
                .set(CouponTemplateDO::getTotalCount, value.totalCount())
                .set(CouponTemplateDO::getValidFrom, value.validFrom())
                .set(CouponTemplateDO::getValidUntil, value.validUntil())
                .set(CouponTemplateDO::getDescription, value.description())
                .set(CouponTemplateDO::getTakeType, value.takeType())
                .set(CouponTemplateDO::getTakeLimitCount, value.takeLimitCount())
                .set(CouponTemplateDO::getProductScope, value.productScope())
                .set(CouponTemplateDO::getValidityType, value.validityType())
                .set(CouponTemplateDO::getFixedStartTerm, value.fixedStartTerm())
                .set(CouponTemplateDO::getFixedEndTerm, value.fixedEndTerm())
                .set(CouponTemplateDO::getProductScopeValues, JSON.toJSONString(value.productScopeValues()))) == 1;
    }

    /** 删除模板后已发券仍通过自身快照使用。 */
    @Override
    public boolean deleteTemplate(Long id) {
        return couponTemplateMapper.deleteById(id) == 1;
    }

    /** 在模板行锁内维护发放和核销计数。 */
    @Override
    public void changeCounts(Long id, int takeDelta, int useDelta) {
        var current = couponTemplateMapper.lockTemplate(id);
        if (current == null) {
            return;
        }
        int takeCount = current.getIssuedCount() + takeDelta;
        int useCount = current.getUseCount() + useDelta;
        if (takeCount < 0 || useCount < 0 || useCount > takeCount) {
            throw new IllegalStateException("优惠券计数不一致");
        }
        couponTemplateMapper.update(null, Wrappers.<CouponTemplateDO>lambdaUpdate()
                .eq(CouponTemplateDO::getId, id).set(CouponTemplateDO::getIssuedCount, takeCount)
                .set(CouponTemplateDO::getUseCount, useCount));
    }

    @Override
    public boolean enableTemplate(Long id, boolean enabled) {
        return couponTemplateMapper.update(null, Wrappers.<CouponTemplateDO>lambdaUpdate()
                .set(CouponTemplateDO::getEnabled, enabled)
                .eq(CouponTemplateDO::getId, id)) == 1;
    }

    @Override
    /**
     * 可领取优惠。
     */
    public List<CouponTemplate> offers(int offset, int limit) {
        return couponTemplateMapper.offers(offset, limit).stream().map(CouponConverter::template).toList();
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
    /** 获取与本域 SQL 写入一致的数据库时间。 */
    @Override
    public LocalDateTime now() {
        return couponTemplateMapper.now();
    }
    /** 领取方式与模板状态分别维护，保持原有领券入口行为。 */
    @Override
    public boolean claimable(Long id) {
        var row = couponTemplateMapper.selectById(id);
        return row != null && "USER".equals(row.getTakeType());
    }

    /** 修改模板领取方式。 */
    @Override
    public void claimable(Long id, boolean enabled) {
        if (couponTemplateMapper.update(null, Wrappers
                .<CouponTemplateDO>lambdaUpdate().eq(CouponTemplateDO::getId, id)
                .set(CouponTemplateDO::getTakeType, enabled ? "USER" : "ADMIN")) != 1) {
            throw new IllegalArgumentException("模板不存在");
        }
    }

    /** 查询开放领取的模板编号。 */
    @Override
    public List<Long> publicTemplateIds() {
        var rows = couponTemplateMapper.selectList(Wrappers
                .<CouponTemplateDO>lambdaQuery().select(CouponTemplateDO::getId)
                .eq(CouponTemplateDO::getTakeType, "USER").orderByAsc(CouponTemplateDO::getId).last("LIMIT 1001"));
        if (rows.size() > 1000) {
            throw new IllegalStateException("公开模板超过1000，请接入分页");
        }
        return rows.stream().map(CouponTemplateDO::getId).toList();
    }
}

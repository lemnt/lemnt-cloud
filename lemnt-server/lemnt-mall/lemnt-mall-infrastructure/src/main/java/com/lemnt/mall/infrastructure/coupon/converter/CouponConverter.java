package com.lemnt.mall.infrastructure.coupon.converter;

import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.coupon.model.CouponTerms;
import com.lemnt.mall.infrastructure.coupon.dataobject.CouponDO;
import com.lemnt.mall.infrastructure.coupon.dataobject.CouponTemplateDO;

import com.alibaba.fastjson2.JSON;

/** 优惠券模板与持券规则快照转换，集合字段仅在基础设施处理 JSON。 */
public final class CouponConverter {
    private CouponConverter() {
    }

    /** 读取模板当前规则及统计。 */
    public static CouponTemplate template(CouponTemplateDO row) {
        return row == null ? null : new CouponTemplate(row.getId(), row.getCode(), row.getName(), row.getKind(),
                row.getThreshold(), row.getFixedAmount(), row.getDiscountPercent(), row.getDiscountLimit(),
                row.getTotalCount(), row.getIssuedCount(), row.getValidFrom(), row.getValidUntil(),
                Boolean.TRUE.equals(row.getEnabled()),
                row.getDescription(),
                row.getTakeType(),
                row.getTakeLimitCount(),
                row.getProductScope(),
                JSON.parseArray(row.getProductScopeValues(), Long.class),
                row.getValidityType(),
                row.getFixedStartTerm(),
                row.getFixedEndTerm(),
                row.getUseCount());
    }

    /** 写入模板，不更改已经发放的会员券。 */
    public static CouponTemplateDO template(CouponTemplate value) {
        var row = new CouponTemplateDO();
        row.setId(value.id());
        row.setCode(value.code());
        row.setName(value.name());
        row.setKind(value.kind());
        row.setThreshold(value.threshold());
        row.setFixedAmount(value.fixedAmount());
        row.setDiscountPercent(value.discountPercent());
        row.setDiscountLimit(value.discountLimit());
        row.setTotalCount(value.totalCount());
        row.setIssuedCount(value.issuedCount());
        row.setValidFrom(value.validFrom());
        row.setValidUntil(value.validUntil());
        row.setEnabled(value.enabled());
        row.setDescription(value.description());
        row.setTakeType(value.takeType());
        row.setTakeLimitCount(value.takeLimitCount());
        row.setProductScope(value.productScope());
        row.setProductScopeValues(JSON.toJSONString(value.productScopeValues()));
        row.setValidityType(value.validityType());
        row.setFixedStartTerm(value.fixedStartTerm());
        row.setFixedEndTerm(value.fixedEndTerm());
        row.setUseCount(value.useCount());
        return row;
    }

    /** 从会员券自身读取规则，不能通过当前模板回填到运行期对象。 */
    public static Coupon coupon(CouponDO row) {
        return row == null ? null : new Coupon(row.getId(), row.getTemplateId(), row.getMemberId(), row.getStatus(),
                row.getOrderNo(), terms(row), row.getUseTime());
    }

    private static CouponTerms terms(CouponDO row) {
        if (row.getName() == null) {
            return null;
        }
        return new CouponTerms(
                row.getCode(),
                row.getName(),
                row.getTakeType(),
                row.getKind(),
                row.getThreshold(),
                row.getFixedAmount(),
                row.getDiscountPercent(),
                row.getDiscountLimit(),
                row.getProductScope(),
                JSON.parseArray(row.getProductScopeValues(), Long.class),
                row.getValidFrom(),
                row.getValidUntil());
    }

    /** 首次发放时把业务规则写入会员券各列。 */
    public static CouponDO coupon(Coupon value) {
        var row = new CouponDO();
        row.setId(value.id());
        row.setTemplateId(value.templateId());
        row.setMemberId(value.memberId());
        row.setStatus(value.status());
        row.setOrderNo(value.orderNo());
        row.setUseTime(value.useTime());
        var terms = value.terms();
        if (terms == null) {
            throw new IllegalArgumentException("发放优惠券必须包含完整规则快照");
        }
        row.setCode(terms.code());
        row.setName(terms.name());
        row.setTakeType(terms.takeType());
        row.setKind(terms.kind());
        row.setThreshold(terms.threshold());
        row.setFixedAmount(terms.fixedAmount());
        row.setDiscountPercent(terms.discountPercent());
        row.setDiscountLimit(terms.discountLimit());
        row.setProductScope(terms.productScope());
        row.setProductScopeValues(JSON.toJSONString(terms.productScopeValues()));
        row.setValidFrom(terms.validFrom());
        row.setValidUntil(terms.validUntil());
        return row;
    }
}

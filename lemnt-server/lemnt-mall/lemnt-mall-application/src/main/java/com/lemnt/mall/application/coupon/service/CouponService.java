package com.lemnt.mall.application.coupon.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.coupon.cmd.CouponClaimCmd;
import com.lemnt.mall.application.coupon.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.coupon.converter.CouponDTOConverter;
import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.application.coupon.dto.CouponOfferDTO;
import com.lemnt.mall.application.coupon.dto.CouponPageDTO;
import com.lemnt.mall.application.coupon.dto.CouponWalletDTO;
import com.lemnt.mall.application.coupon.dto.CouponWalletPageDTO;
import com.lemnt.mall.application.coupon.qry.CouponPageQry;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 优惠券领取与查询编排。 */
@Component
@RequiredArgsConstructor
public class CouponService {
    private final CouponIssuanceService couponIssuanceService;
    private final CouponGateway couponGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final MemberValidationService memberValidationService;

    private void page(CouponPageQry q) {
        if (q == null
                || q.pageNum() < 1
                || q.pageNum() > 1000000
                || q.pageSize() < 1
                || q.pageSize() > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        memberValidationService.requireActive(q.memberId());
    }

    private boolean valid(CouponTemplate t) {
        var now = couponTemplateGateway.now();
        return t != null
                && t.enabled()
                && ("TERM".equals(t.validityType()) || t.validUntil().isAfter(now));
    }

    private CouponOfferDTO offer(CouponTemplate t, Coupon c, boolean publicClaim) {
        var now = couponTemplateGateway.now();
        return new CouponOfferDTO(
                CouponDTOConverter.template(t),
                c == null ? null : c.id(),
                c == null ? null : c.status(),
                c != null && "AVAILABLE".equals(c.status()) && c.terms() != null
                        && !now.isBefore(c.terms().validFrom()) && now.isBefore(c.terms().validUntil()),
                publicClaim && valid(t) && (t.totalCount() == -1 || t.issuedCount() < t.totalCount())
                        && (t.takeLimitCount() == -1 || (c == null ? 0 : couponGateway.takeCount(c.memberId(), t.id())) < t.takeLimitCount()));
    }

    /** 分页查询当前会员可领取的优惠券。 */
    @Transactional(readOnly = true)
    public CouponPageDTO offers(CouponPageQry q) {
        page(q);
        return new CouponPageDTO(
                couponTemplateGateway.offers((q.pageNum() - 1) * q.pageSize(), q.pageSize()).stream()
                .map(t -> offer(t, couponGateway.issued(q.memberId(), t.id()), true))
                .toList(),
                couponTemplateGateway.offerCount());
    }

    /** 分页查询当前会员的优惠券包。 */
    @Transactional(readOnly = true)
    public CouponWalletPageDTO wallet(CouponPageQry q) {
        page(q);
        var now = couponTemplateGateway.now();
        var values = couponGateway.wallet(q.memberId(), (q.pageNum() - 1) * q.pageSize(), q.pageSize()).stream()
                .map(c -> {
                    var terms = c.terms();
                    if (terms == null) {
                        throw new ServiceException("优惠券规则尚未完成数据迁移", 409);
                    }
                    return new CouponWalletDTO(c.id(), c.templateId(),
                            c.status(), CouponDTOConverter.terms(terms), "AVAILABLE".equals(c.status())
                            && !now.isBefore(terms.validFrom()) && now.isBefore(terms.validUntil()));
                }).toList();
        return new CouponWalletPageDTO(values, couponGateway.walletCount(q.memberId()));
    }

    /**
     * 公开编号集合。
     */
    public List<Long> publicIds() {
        return couponTemplateGateway.publicTemplateIds();
    }

    /** 设置模板是否开放领取。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean publicClaim(CouponPublicClaimCmd c) {
        CouponRules.id(c.templateId());
        var template = couponTemplateGateway.lockTemplate(c.templateId());
        if (template == null) {
            throw new IllegalArgumentException("模板不存在");
        }
        if (c.enabled() && template.totalCount() != -1 && template.issuedCount() > template.totalCount()) {
            throw new ServiceException("公开领取总量不能小于已发数量", 400);
        }
        couponTemplateGateway.claimable(c.templateId(), c.enabled());
        return true;
    }

    /** 在会员操作锁内校验领取资格并幂等发券。 */
    @Transactional(rollbackFor = Exception.class)
    public CouponDTO claim(CouponClaimCmd c) {
        memberValidationService.requireActive(c.memberId());
        CouponRules.id(c.templateId());
        return CouponDTOConverter.coupon(couponIssuanceService.take(c.memberId(), c.templateId(), "USER"));
    }
}

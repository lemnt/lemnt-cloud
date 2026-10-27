package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.promotion.model.DiscountRule;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.domain.promotion.model.PromotionScope;
import com.lemnt.mall.domain.promotion.model.PromotionUsage;
import com.lemnt.mall.domain.promotion.model.PromotionVersion;
import com.lemnt.mall.domain.promotion.model.PublishedPromotion;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRootDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionUsageDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionVersionDO;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Closed internal document; not HTTP JSON and no polymorphic type activation.
 */
public final class PromotionConverter {
    private PromotionConverter() {}

    private static final JsonMapper JSON =
    JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    /**
     * 文档。
     */
    public record Document(
            String name,
            String scopeKind,
            List<Long> ids,
            String ruleKind,
            List<Tier> tiers,
            Long thresholdFen,
            Integer payRateBps,
            Long maxDiscountFen,
            String couponPolicy,
            int priority,
            long startsAtEpochMs,
            long endsAtEpochMs,
            Long globalUseLimit,
            Long memberUseLimit
    ) {}

    /**
     * 阶梯。
     */
    public record Tier(
            long thresholdFen,
            long discountFen
    ) {}

    /**
     * 编码。
     */
    public static String encode(PromotionDraft d) {
        var p = d.rule() instanceof DiscountRule.Percentage percent ? percent : null;
        var t =
        d.rule() instanceof DiscountRule.FullReduction f
        ? f.tiers().stream()
            .map(x -> new Tier(x.thresholdFen(), x.discountFen()))
            .toList()
        : null;
        return JSON.writeValueAsString(
                new Document(
                d.name(),
                d.scope().kind().name(),
                d.scope().ids(),
                p == null ? "FULL_REDUCTION" : "ORDER_DISCOUNT",
                t,
                p == null ? null : p.thresholdFen(),
                p == null ? null : p.payRateBps(),
                p == null ? null : p.maxDiscountFen(),
                d.couponPolicy().name(),
                d.priority(),
                d.startsAtEpochMs(),
                d.endsAtEpochMs(),
                d.globalUseLimit(),
                d.memberUseLimit()));
    }

    /**
     * 解码。
     */
    public static PromotionDraft decode(String text) {
        if (text == null || text.length() > 1048576) {
            throw new IllegalStateException("Invalid stored promotion document");
        }
        var d = JSON.readValue(text, Document.class);
        DiscountRule rule;
        if ("FULL_REDUCTION".equals(d.ruleKind())) {
            PricingException.require(
                    d.tiers() != null
                    && d.thresholdFen() == null
                    && d.payRateBps() == null
                    && d.maxDiscountFen() == null);
            rule =
            new DiscountRule.FullReduction(
                    d.tiers().stream()
                    .map(
                    t ->
                    new DiscountRule.Tier(
                    t.thresholdFen(), t.discountFen()))
                    .toList());
        } else if ("ORDER_DISCOUNT".equals(d.ruleKind())) {
            PricingException.require(
                    d.tiers() == null && d.thresholdFen() != null && d.payRateBps() != null);
            rule =
            new DiscountRule.Percentage(
                    d.thresholdFen(), d.payRateBps(), d.maxDiscountFen());
        } else {
            throw new IllegalStateException("Unknown stored promotion rule");
        }
        return new PromotionDraft(
                d.name(),
                new PromotionScope(PromotionScope.Kind.valueOf(d.scopeKind()), d.ids()),
                rule,
                PromotionVersion.CouponPolicy.valueOf(d.couponPolicy()),
                d.priority(),
                d.startsAtEpochMs(),
                d.endsAtEpochMs(),
                d.globalUseLimit(),
                d.memberUseLimit());
    }

    /**
     * 摘要。
     */
    public static String hash(PromotionDraft d) {
        return CheckoutRules.fingerprint("promo-1|" + encode(d));
    }

    /**
     * 根节点。
     */
    public static PromotionRoot root(PromotionRootDO r) {
        return r == null
        ? null
        : new PromotionRoot(
                r.getId(),
                r.getCode(),
                PromotionRoot.State.valueOf(r.getState()),
                r.getRevision(),
                r.getActiveVersion(),
                r.getLastVersion(),
                decode(r.getDraftJson()));
    }

    /**
     * 转换为持久化数据对象。
     */
    public static PromotionRootDO row(PromotionRoot r) {
        var d = new PromotionRootDO();
        d.setId(r.id());
        d.setCode(r.code());
        d.setState(r.state().name());
        d.setRevision(r.revision());
        d.setActiveVersion(r.activeVersion());
        d.setLastVersion(r.lastVersion());
        d.setDraftJson(encode(r.draft()));
        return d;
    }

    /**
     * 版本。
     */
    public static PublishedPromotion version(PromotionVersionDO r) {
        if (r == null) {
            return null;
        }
        var d = decode(r.getDraftJson());
        if (!"promo-1".equals(r.getAlgorithmVersion())
                || !CheckoutRules.fingerprint(r.getRootId() + ":" + r.getVersion() + ":" + hash(d))
                .equals(r.getRuleHash())
                || d.startsAtEpochMs() != r.getStartsAtEpochMs()
                || d.endsAtEpochMs() != r.getEndsAtEpochMs()) {
            throw new IllegalStateException("Stored promotion integrity mismatch");
        }
        return new PublishedPromotion(
                r.getRootId(),
                r.getVersion(),
                d,
                r.getRuleHash(),
                r.getAlgorithmVersion(),
                r.getOperatorId(),
                r.getPublishedAtEpochMs());
    }

    /**
     * 使用。
     */
    public static PromotionUsage usage(PromotionUsageDO r) {
        return r == null
        ? null
        : new PromotionUsage(
                r.getOrderNo(),
                r.getRootId(),
                r.getVersion(),
                r.getMemberId(),
                r.getDiscountFen(),
                r.getRuleHash(),
                PromotionUsage.State.valueOf(r.getState()));
    }

    /**
     * 转换为持久化数据对象。
     */
    public static PromotionUsageDO row(PromotionUsage u) {
        var r = new PromotionUsageDO();
        r.setOrderNo(u.orderNo());
        r.setRootId(u.rootId());
        r.setVersion(u.version());
        r.setMemberId(u.memberId());
        r.setDiscountFen(u.discountFen());
        r.setRuleHash(u.ruleHash());
        r.setState(u.state().name());
        return r;
    }
}

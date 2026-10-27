package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.req.PromotionCreateReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionDraftReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionDraftSaveReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionPageReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionPublishReq;
import com.lemnt.mall.adapter.web.promotion.req.PromotionStateChangeReq;
import com.lemnt.mall.application.promotion.cmd.PromotionCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionDraftSaveCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionPublishCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionStateChangeCmd;
import com.lemnt.mall.application.promotion.qry.PromotionPageQry;
import com.lemnt.mall.domain.promotion.model.DiscountRule;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.domain.promotion.model.PromotionScope;
import com.lemnt.mall.domain.promotion.model.PromotionVersion;

import java.util.Map;
import java.util.Set;

/**
 * 营销请求转换器。
 */
public final class PromotionReqConverter {
    private PromotionReqConverter() {}

    /**
     * 编号。
     */
    public static long id(String value) {
        return number(value, true);
    }

    private static long number(String value, boolean positive) {
        PricingException.require(
                value != null
                && value.matches(positive ? "[1-9][0-9]{0,18}" : "0|[1-9][0-9]{0,18}"));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new PricingException(PricingException.Code.AMOUNT_OUT_OF_RANGE);
        }
    }

    private static Long optional(String value) {
        return value == null ? null : number(value, true);
    }

    /**
     * 草稿。
     */
    public static PromotionDraft draft(PromotionDraftReq d) {
        DiscountRule rule;
        if ("FULL_REDUCTION".equals(d.type())) {
            PricingException.require(d.rule().tiers() != null);
            rule =
            new DiscountRule.FullReduction(
                    d.rule().tiers().stream()
                    .map(
                    t ->
                    new DiscountRule.Tier(
                    number(t.thresholdFen(), false),
                    number(t.discountFen(), true)))
                    .toList());
        } else {
            PricingException.require(
                    d.rule().thresholdFen() != null && d.rule().payRateBps() != null);
            rule =
            new DiscountRule.Percentage(
                    number(d.rule().thresholdFen(), false),
                    d.rule().payRateBps(),
                    optional(d.rule().maxDiscountFen()));
        }
        return new PromotionDraft(
                d.name(),
                new PromotionScope(
                PromotionScope.Kind.valueOf(d.scope().kind()),
                d.scope().ids().stream().map(PromotionReqConverter::id).toList()),
                rule,
                PromotionVersion.CouponPolicy.valueOf(d.couponPolicy()),
                d.priority(),
                id(d.startsAtEpochMs()),
                id(d.endsAtEpochMs()),
                optional(d.globalUseLimit()),
                optional(d.memberUseLimit()));
    }

    /**
     * 创建。
     */
    public static PromotionCreateCmd create(long operator, PromotionCreateReq r) {
        return new PromotionCreateCmd(operator, r.requestNo(), r.code(), draft(r.draft()));
    }

    /**
     * 保存。
     */
    public static PromotionDraftSaveCmd save(long operator, String id, PromotionDraftSaveReq r) {
        return new PromotionDraftSaveCmd(
                operator, r.requestNo(), id(id), r.expectedRevision(), draft(r.draft()));
    }

    /**
     * 发布。
     */
    public static PromotionPublishCmd publish(long operator, String id, PromotionPublishReq r) {
        return new PromotionPublishCmd(operator, r.requestNo(), id(id), r.expectedRevision());
    }

    /**
     * 状态。
     */
    public static PromotionStateChangeCmd state(
            long operator, String id, PromotionStateChangeReq r) {
        var target =
        switch (r.action()) {
            case "PAUSE" -> PromotionRoot.State.PAUSED;
            case "RESUME" -> PromotionRoot.State.PUBLISHED;
            case "ARCHIVE" -> PromotionRoot.State.ARCHIVED;
            default ->
            throw new PricingException(
                    PricingException.Code.INVALID_PROMOTION_REQUEST);
        };
        return new PromotionStateChangeCmd(
                operator, r.requestNo(), id(id), r.expectedRevision(), target);
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static PromotionPageQry page(PromotionPageReq r, Map<String, String[]> params) {
        PricingException.require(
                params.keySet().stream()
                .allMatch(
                Set.of("pageNum", "pageSize", "state", "code")
                ::contains)
                && params.values().stream().allMatch(v -> v.length == 1));
        return new PromotionPageQry(
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize(),
                r.state(),
                r.code());
    }
}

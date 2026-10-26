package com.lemnt.trade.adapter.web.promotion.converter;

import com.lemnt.trade.adapter.web.promotion.req.*;
import com.lemnt.trade.application.promotion.cmd.*;
import com.lemnt.trade.application.promotion.qry.PromotionPageQry;
import com.lemnt.trade.domain.promotion.model.*;
import java.util.*;

public final class PromotionReqConverter {
    private PromotionReqConverter() {}

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

    public static PromotionCreateCmd create(long operator, PromotionCreateReq r) {
        return new PromotionCreateCmd(operator, r.requestNo(), r.code(), draft(r.draft()));
    }

    public static PromotionDraftSaveCmd save(long operator, String id, PromotionDraftSaveReq r) {
        return new PromotionDraftSaveCmd(
                operator, r.requestNo(), id(id), r.expectedRevision(), draft(r.draft()));
    }

    public static PromotionPublishCmd publish(long operator, String id, PromotionPublishReq r) {
        return new PromotionPublishCmd(operator, r.requestNo(), id(id), r.expectedRevision());
    }

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

    public static PromotionPageQry page(PromotionPageReq r, Map<String, String[]> params) {
        PricingException.require(
                params.keySet().stream()
                                .allMatch(
                                        java.util.Set.of("pageNum", "pageSize", "state", "code")
                                                ::contains)
                        && params.values().stream().allMatch(v -> v.length == 1));
        return new PromotionPageQry(
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize(),
                r.state(),
                r.code());
    }
}

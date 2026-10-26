package com.lemnt.trade.adapter.web.promotion.converter;

import com.lemnt.trade.adapter.web.promotion.res.*;
import com.lemnt.trade.application.promotion.dto.*;

public final class PromotionResConverter {
    private PromotionResConverter() {}

    private static String text(Long n) {
        return n == null ? null : n.toString();
    }

    public static PromotionResultRes result(PromotionResultDTO r) {
        return new PromotionResultRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }

    public static PromotionDraftRes draft(PromotionDraftDTO d) {
        if (d == null) return null;
        PromotionRuleRes rule =
                "FULL_REDUCTION".equals(d.type())
                        ? new PromotionFullReductionRes(
                                d.tiers().stream()
                                        .map(
                                                t ->
                                                        new PromotionTierRes(
                                                                Long.toString(t.thresholdFen()),
                                                                Long.toString(t.discountFen())))
                                        .toList())
                        : new PromotionOrderDiscountRes(
                                text(d.thresholdFen()), d.payRateBps(), text(d.maxDiscountFen()));
        return new PromotionDraftRes(
                d.name(),
                d.type(),
                new PromotionScopeRes(
                        d.scopeKind(), d.scopeIds().stream().map(Object::toString).toList()),
                "ALL_AUTHENTICATED",
                d.couponPolicy(),
                d.priority(),
                Long.toString(d.startsAtEpochMs()),
                Long.toString(d.endsAtEpochMs()),
                text(d.globalUseLimit()),
                text(d.memberUseLimit()),
                rule);
    }

    public static PromotionDetailRes detail(PromotionDetailDTO d) {
        return new PromotionDetailRes(
                Long.toString(d.id()),
                d.revision(),
                d.state(),
                d.activeVersion(),
                d.code(),
                draft(d.draft()),
                draft(d.activeRule()),
                d.timePhase());
    }

    public static PromotionPageRes page(PromotionPageDTO p) {
        return new PromotionPageRes(
                p.records().stream().map(PromotionResConverter::result).toList(),
                Long.toString(p.total()));
    }
}

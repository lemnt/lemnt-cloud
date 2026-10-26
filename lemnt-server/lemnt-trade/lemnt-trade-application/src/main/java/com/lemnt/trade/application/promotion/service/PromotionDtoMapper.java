package com.lemnt.trade.application.promotion.service;

import com.lemnt.trade.application.promotion.dto.*;
import com.lemnt.trade.domain.promotion.model.*;

public final class PromotionDtoMapper {
    private PromotionDtoMapper() {}

    public static PromotionResultDTO result(PromotionAdminReceipt r) {
        return new PromotionResultDTO(
                r.id(), Math.toIntExact(r.revision()), r.state().name(), r.activeVersion());
    }

    public static PromotionDraftDTO draft(PromotionDraft d) {
        if (d == null) return null;
        var p = d.rule() instanceof DiscountRule.Percentage x ? x : null;
        var tiers =
                d.rule() instanceof DiscountRule.FullReduction f
                        ? f.tiers().stream()
                                .map(t -> new PromotionTierDTO(t.thresholdFen(), t.discountFen()))
                                .toList()
                        : null;
        return new PromotionDraftDTO(
                d.name(),
                p == null ? "FULL_REDUCTION" : "ORDER_DISCOUNT",
                d.scope().kind().name(),
                d.scope().ids(),
                d.couponPolicy().name(),
                d.priority(),
                d.startsAtEpochMs(),
                d.endsAtEpochMs(),
                d.globalUseLimit(),
                d.memberUseLimit(),
                tiers,
                p == null ? null : p.thresholdFen(),
                p == null ? null : p.payRateBps(),
                p == null ? null : p.maxDiscountFen());
    }
}

package com.lemnt.mall.application.promotion.service;

import com.lemnt.mall.application.promotion.dto.PromotionDraftDTO;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;
import com.lemnt.mall.application.promotion.dto.PromotionTierDTO;
import com.lemnt.mall.domain.promotion.model.DiscountRule;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionReceipt;

/**
 * 营销数据传输对象数据访问接口。
 */
public final class PromotionDtoMapper {
    private PromotionDtoMapper() {}

    /**
     * 结果。
     */
    public static PromotionResultDTO result(PromotionReceipt r) {
        return new PromotionResultDTO(
                r.id(), Math.toIntExact(r.revision()), r.state().name(), r.activeVersion());
    }

    /**
     * 草稿。
     */
    public static PromotionDraftDTO draft(PromotionDraft d) {
        if (d == null) {
            return null;
        }
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

package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.res.MallPromotionCreateRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionPublishRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionSaveRes;
import com.lemnt.mall.adapter.web.promotion.res.MallPromotionStateRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionDraftRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionFullReductionRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionOrderDiscountRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionPageItemRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionRuleRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionScopeRes;
import com.lemnt.mall.adapter.web.promotion.res.PromotionTierRes;
import com.lemnt.mall.application.promotion.dto.PromotionDetailDTO;
import com.lemnt.mall.application.promotion.dto.PromotionDraftDTO;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;

/**
 * 营销响应转换器。
 */
public final class PromotionResConverter {
    private PromotionResConverter() {}

    private static String text(Long n) {
        return n == null ? null : n.toString();
    }

    /**
     * 草稿。
     */
    public static PromotionDraftRes draft(PromotionDraftDTO d) {
        if (d == null) {
            return null;
        }
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

    /**
     * 详情。
     */
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

    /**
     * 结果。
     */
    public static MallPromotionCreateRes resultToMallPromotionCreateRes(PromotionResultDTO r) {
        return new MallPromotionCreateRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }

    /**
     * 结果。
     */
    public static MallPromotionSaveRes resultToMallPromotionSaveRes(PromotionResultDTO r) {
        return new MallPromotionSaveRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }

    /**
     * 结果。
     */
    public static MallPromotionPublishRes resultToMallPromotionPublishRes(PromotionResultDTO r) {
        return new MallPromotionPublishRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }

    /**
     * 结果。
     */
    public static MallPromotionStateRes resultToMallPromotionStateRes(PromotionResultDTO r) {
        return new MallPromotionStateRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }

    /** 转换活动分页摘要。 */
    public static PromotionPageItemRes pageItem(PromotionResultDTO r) {
        return new PromotionPageItemRes(
                Long.toString(r.id()), r.revision(), r.state(), r.activeVersion());
    }
}

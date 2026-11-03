package com.lemnt.promotion.adapter.web.promotion.converter;

import com.lemnt.promotion.adapter.web.promotion.req.RewardActivityCreateReq;
import com.lemnt.promotion.adapter.web.promotion.req.RewardActivityPageReq;
import com.lemnt.promotion.adapter.web.promotion.req.RewardActivityUpdateReq;
import com.lemnt.promotion.application.promotion.cmd.RewardActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.RewardActivityUpdateCmd;
import com.lemnt.promotion.application.promotion.qry.RewardActivityPageQry;
import com.lemnt.promotion.domain.promotion.model.RewardRule;

/** 活动请求转换。 */
public final class RewardActivityReqConverter {
    private RewardActivityReqConverter() {}

    public static RewardActivityCreateCmd create(RewardActivityCreateReq req) {
        return new RewardActivityCreateCmd(
                req.name(),
                req.startTime(),
                req.endTime(),
                req.remark(),
                req.conditionType(),
                req.productScope(),
                req.productScopeValues(),
                req.rules().stream()
                        .map(
                                r ->
                                        new RewardRule(
                                                r.limit(),
                                                r.discountPrice(),
                                                r.freeDelivery(),
                                                r.point() == null ? 0 : r.point(),
                                                r.giveCouponTemplateCounts()))
                        .toList());
    }

    public static RewardActivityUpdateCmd update(Long id, RewardActivityUpdateReq req) {
        return new RewardActivityUpdateCmd(
                id,
                req.name(),
                req.startTime(),
                req.endTime(),
                req.remark(),
                req.conditionType(),
                req.productScope(),
                req.productScopeValues(),
                req.rules().stream()
                        .map(
                                r ->
                                        new RewardRule(
                                                r.limit(),
                                                r.discountPrice(),
                                                r.freeDelivery(),
                                                r.point() == null ? 0 : r.point(),
                                                r.giveCouponTemplateCounts()))
                        .toList());
    }

    public static RewardActivityPageQry page(RewardActivityPageReq req) {
        return new RewardActivityPageQry(
                req.name(),
                req.status(),
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}

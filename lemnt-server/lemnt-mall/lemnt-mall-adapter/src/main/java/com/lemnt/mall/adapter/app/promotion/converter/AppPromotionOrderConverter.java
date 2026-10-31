package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.req.AppPromotionOrderBatchCreateReq;
import com.lemnt.mall.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.mall.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderLineCmd;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

/** App 活动下单请求及响应转换。 */
public final class AppPromotionOrderConverter {
    private AppPromotionOrderConverter() {}

    public static PromotionOrderCmd cmd(Long memberId, String type, AppPromotionOrderCreateReq req) {
        return new PromotionOrderCmd(memberId, type, req.activityId(), req.recordId(), req.headId(),
                req.skuId(), req.quantity(), req.addressId(), req.requestNo());
    }

    public static PromotionOrderCmd batchCmd(Long memberId, String type, AppPromotionOrderBatchCreateReq req) {
        return PromotionOrderCmd.batch(memberId, type, req.activityId(), null, null,
                req.lines().stream().map(line -> new PromotionOrderLineCmd(line.skuId(), line.quantity()))
                        .toList(), req.addressId(), req.requestNo());
    }

    public static AppPromotionOrderRes res(SettlementOrder order) {
        return new AppPromotionOrderRes(order.orderNo(), order.totalAmount(), order.status(),
                order.usePoint(), order.givePoint());
    }
}

package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.dto.PreparedPromotionOrder;
import com.lemnt.mall.application.promotion.dto.PromotionOrderDraft;
import com.lemnt.mall.application.settlement.dto.SettlementPreparationResult;
import com.lemnt.mall.application.settlement.service.SettlementPreparationService;
import com.lemnt.mall.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/** 活动订单本地准备：请求幂等、结算快照和活动订单映射在同一事务内保存。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderPreparationService {
    private final PromotionOrderGateway promotionOrders;
    private final PromotionOrderPricingService pricing;
    private final SettlementPreparationService settlements;
    private final OrderGateway orders;

    /** 同请求编号返回原快照；活动已关闭时仍可重试原请求。 */
    @Transactional(rollbackFor = Exception.class)
    public PreparedPromotionOrder prepare(PromotionOrderCmd cmd, String fingerprint) {
        var previous = promotionOrders.findByRequest(cmd.memberId(), cmd.requestNo());
        if (previous != null && matches(previous, cmd)) {
            var order = orders.findByOrderNo(previous.orderNo());
            if (order == null) {
                order = settlements.preparePromotion(cmd.memberId(), cmd.requestNo(), fingerprint,
                        () -> {
                            throw new IllegalStateException("原提交快照已失效，请查询交易订单");
                        }, List.of()).order();
            }
            if ("FAILED".equals(previous.status())) {
                promotionOrders.reopenFailed(previous.orderNo());
                previous = promotionOrders.findByOrderNo(previous.orderNo());
            }
            return new PreparedPromotionOrder(order, previous, false);
        }
        if (previous != null) {
            throw new ServiceException("请求编号已用于不同的活动订单内容", 409);
        }
        PromotionOrderDraft draft = pricing.price(cmd);
        SettlementPreparationResult result = settlements.preparePromotion(cmd.memberId(), cmd.requestNo(),
                fingerprint, draft::quote, List.of());
        if (!result.created()) {
            var existing = promotionOrders.findByRequest(cmd.memberId(), cmd.requestNo());
            if (existing == null || !matches(existing, cmd)) {
                throw new ServiceException("请求编号已用于不同的活动订单内容", 409);
            }
            return new PreparedPromotionOrder(result.order(), existing, false);
        }
        try {
            var saved = promotionOrders.save(new PromotionOrder(null, result.order().orderNo(),
                    cmd.requestNo(), cmd.memberId(), draft.activityType(), draft.activityId(),
                    draft.recordId(), draft.headId(), draft.skuId(), draft.quantity(),
                    draft.activityPrice(), draft.usePoint(), 0, "CREATED", null, null, null));
            return new PreparedPromotionOrder(result.order(), saved, true);
        } catch (DuplicateKeyException exception) {
            var existing = promotionOrders.findByRequest(cmd.memberId(), cmd.requestNo());
            if (existing == null || !matches(existing, cmd)) {
                throw exception;
            }
            return new PreparedPromotionOrder(result.order(), existing, false);
        }
    }

    private boolean matches(PromotionOrder value, PromotionOrderCmd cmd) {
        if (!Objects.equals(value.activityType(), cmd.activityType())
                || !Objects.equals(value.skuId(), cmd.skuId())
                || !Objects.equals(value.quantity(), cmd.quantity())) {
            return false;
        }
        if ("BARGAIN".equals(cmd.activityType())) {
            return Objects.equals(value.activityRecordId(), cmd.recordId());
        }
        return Objects.equals(value.activityId(), cmd.activityId())
                && Objects.equals(value.headId(), cmd.headId());
    }
}

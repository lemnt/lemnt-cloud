package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.dto.PromotionOrderDraft;
import com.lemnt.mall.application.settlement.service.SettlementDeliveryService;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.product.model.ProductSnapshot;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/** 活动订单计价：只使用活动价，不叠加优惠券、满减、折扣或会员折扣。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderPricingService {
    private final ProductCatalogGateway productCatalogGateway;
    private final MemberValidationService memberValidationService;
    private final MemberTradeGateway memberTradeGateway;
    private final SettlementDeliveryService deliveryService;
    private final SeckillStockService seckillStockService;
    private final CombinationRecordService combinationRecordService;
    private final BargainRecordService bargainRecordService;
    private final PointActivityManagementService pointActivityManagementService;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    /** 校验活动并生成订单行快照，实付金额与消耗积分由服务端决定。 */
    public PromotionOrderDraft price(PromotionOrderCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        require(cmd.skuId() != null && cmd.skuId() > 0, "SKU 编号不合法");
        require(cmd.quantity() != null && cmd.quantity() > 0 && cmd.quantity() <= 999, "购买数量不合法");
        require(cmd.activityType() != null, "活动类型不能为空");
        var member = memberValidationService.requireActive(cmd.memberId());
        var snapshot = productCatalogGateway.findOnShelfBatch(List.of(cmd.skuId())).get(cmd.skuId());
        require(snapshot != null && snapshot.stock() != null && snapshot.stock() >= cmd.quantity(),
                "商品已下架或库存不足");

        var type = cmd.activityType();
        var activityId = cmd.activityId();
        var recordId = cmd.recordId();
        var headId = cmd.headId();
        var price = BigDecimal.ZERO;
        long usePoint = 0L;
        switch (type) {
            case "SECKILL" -> {
                require(activityId != null && activityId > 0, "秒杀活动编号不能为空");
                price = seckillStockService.validateJoin(activityId, cmd.skuId(), cmd.quantity()).seckillPrice();
            }
            case "COMBINATION" -> {
                require(activityId != null && activityId > 0, "拼团活动编号不能为空");
                var product = combinationRecordService.validateJoin(cmd.memberId(), activityId, headId,
                        cmd.skuId(), cmd.quantity());
                price = product.combinationPrice();
            }
            case "BARGAIN" -> {
                require(recordId != null && recordId > 0, "砍价记录编号不能为空");
                require(cmd.quantity() == 1, "砍价商品只能购买一件");
                var join = bargainRecordService.validateJoin(cmd.memberId(), recordId, cmd.skuId());
                activityId = join.activityId();
                price = join.bargainPrice();
            }
            case "POINT" -> {
                require(activityId != null && activityId > 0, "积分商城活动编号不能为空");
                var join = pointActivityManagementService.validateJoin(activityId, cmd.skuId(), cmd.quantity());
                price = join.price();
                usePoint = (long) join.point() * cmd.quantity();
                var memberPoints = member.points() == null ? 0L : member.points();
                require(usePoint <= memberPoints, "会员积分不足");
            }
            default -> throw new ServiceException("活动类型不合法", 400);
        }
        var payable = price.multiply(BigDecimal.valueOf(cmd.quantity())).setScale(2, RoundingMode.DOWN);
        require(payable.signum() >= 0 && (payable.signum() > 0 || usePoint > 0),
                "活动订单金额和积分不能同时为零");
        var unit = snapshot.price();
        var lineAmount = unit.multiply(BigDecimal.valueOf(cmd.quantity())).setScale(2, RoundingMode.DOWN);
        require(payable.compareTo(lineAmount) <= 0, "活动价不能高于原价");
        var properties = snapshot.properties() == null ? null : snapshot.properties().stream()
                .map(value -> new SettlementItem.Property(value.propertyId(), value.propertyName(),
                        value.valueId(), value.valueName())).toList();
        var item = new SettlementItem(cmd.skuId(), snapshot.productCode(), snapshot.name(), unit,
                cmd.quantity(), lineAmount, payable, BigDecimal.ZERO.setScale(2), snapshot.spuId(),
                snapshot.spuName(), properties, snapshot.picUrl());
        var config = memberTradeGateway.config();
        long givePoint = config.enabled() && config.givePerYuan() > 0
                ? payable.multiply(BigDecimal.valueOf(config.givePerYuan()))
                    .setScale(0, RoundingMode.DOWN).longValueExact() : 0L;
        SettlementQuote quote = new SettlementQuote(List.of(item), lineAmount, lineAmount.subtract(payable),
                payable, null, usePoint, givePoint, null);
        if (cmd.addressId() != null) {
            quote = deliveryService.attach(cmd.memberId(), cmd.addressId(),
                    Map.of(cmd.skuId(), snapshot), quote);
        }
        return new PromotionOrderDraft(cmd, type, activityId, recordId, headId, cmd.skuId(), cmd.quantity(),
                price, usePoint, quote);
    }
}

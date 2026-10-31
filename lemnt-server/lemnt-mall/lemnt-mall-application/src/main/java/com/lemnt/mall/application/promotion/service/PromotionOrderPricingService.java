package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderLineCmd;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 活动订单计价：只使用活动价，积分商城支持多商品按行计算积分。 */
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
        require(cmd.activityType() != null, "活动类型不能为空");
        var lines = cmd.lines();
        require(!lines.isEmpty(), "活动商品不能为空");
        if (!"POINT".equals(cmd.activityType())) {
            require(lines.size() == 1, "该活动只允许一个商品");
        }
        for (var line : lines) {
            require(line != null && line.skuId() != null && line.skuId() > 0, "SKU 编号不合法");
            require(line.quantity() != null && line.quantity() > 0 && line.quantity() <= 999,
                    "购买数量不合法");
        }
        var member = memberValidationService.requireActive(cmd.memberId());
        var skuIds = lines.stream().map(PromotionOrderLineCmd::skuId).distinct().toList();
        require(skuIds.size() == lines.size(), "商品行不能重复");
        var snapshots = productCatalogGateway.findOnShelfBatch(skuIds);
        for (var line : lines) {
            var snapshot = snapshots.get(line.skuId());
            require(snapshot != null && snapshot.stock() != null && snapshot.stock() >= line.quantity(),
                    "商品已下架或库存不足");
        }

        var type = cmd.activityType();
        var activityId = cmd.activityId();
        var recordId = cmd.recordId();
        var headId = cmd.headId();
        var items = new ArrayList<SettlementItem>();
        var drafts = new ArrayList<PromotionOrderDraft.Line>();
        var original = BigDecimal.ZERO;
        var payable = BigDecimal.ZERO;
        long usePoint = 0L;
        BigDecimal firstPrice = BigDecimal.ZERO;
        for (int index = 0; index < lines.size(); index++) {
            var line = lines.get(index);
            var snapshot = snapshots.get(line.skuId());
            BigDecimal price;
            long lineUsePoint = 0L;
            switch (type) {
                case "SECKILL" -> {
                    require(activityId != null && activityId > 0, "秒杀活动编号不能为空");
                    price = seckillStockService.validateJoin(activityId, line.skuId(), line.quantity())
                            .seckillPrice();
                }
                case "COMBINATION" -> {
                    require(activityId != null && activityId > 0, "拼团活动编号不能为空");
                    price = combinationRecordService.validateJoin(cmd.memberId(), activityId, headId,
                            line.skuId(), line.quantity()).combinationPrice();
                }
                case "BARGAIN" -> {
                    require(recordId != null && recordId > 0, "砍价记录编号不能为空");
                    require(line.quantity() == 1, "砍价商品只能购买一件");
                    var join = bargainRecordService.validateJoin(cmd.memberId(), recordId, line.skuId());
                    activityId = join.activityId();
                    price = join.bargainPrice();
                }
                case "POINT" -> {
                    require(activityId != null && activityId > 0, "积分商城活动编号不能为空");
                    var join = pointActivityManagementService.validateJoin(activityId, line.skuId(),
                            line.quantity());
                    price = join.price();
                    lineUsePoint = (long) join.point() * line.quantity();
                }
                default -> throw new ServiceException("活动类型不合法", 400);
            }
            var unit = snapshot.price();
            var lineAmount = unit.multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(2, RoundingMode.DOWN);
            var linePayable = price.multiply(BigDecimal.valueOf(line.quantity()))
                    .setScale(2, RoundingMode.DOWN);
            require(linePayable.signum() >= 0 && linePayable.compareTo(lineAmount) <= 0,
                    "活动价不能为负或高于原价");
            var properties = snapshot.properties() == null ? null : snapshot.properties().stream()
                    .map(value -> new SettlementItem.Property(value.propertyId(), value.propertyName(),
                            value.valueId(), value.valueName())).toList();
            items.add(new SettlementItem(line.skuId(), snapshot.productCode(), snapshot.name(), unit,
                    line.quantity(), lineAmount, linePayable, BigDecimal.ZERO.setScale(2),
                    snapshot.spuId(), snapshot.spuName(), properties, snapshot.picUrl()));
            drafts.add(new PromotionOrderDraft.Line(line.skuId(), line.quantity(), price, lineUsePoint));
            original = original.add(lineAmount);
            payable = payable.add(linePayable);
            usePoint += lineUsePoint;
            if (index == 0) {
                firstPrice = price;
            }
        }
        require(payable.signum() >= 0 && (payable.signum() > 0 || usePoint > 0),
                "活动订单金额和积分不能同时为零");
        var memberPoints = member.points() == null ? 0L : member.points();
        require(usePoint <= memberPoints, "会员积分不足");
        var config = memberTradeGateway.config();
        long givePoint = config.enabled() && config.givePerYuan() > 0
                ? payable.multiply(BigDecimal.valueOf(config.givePerYuan()))
                    .setScale(0, RoundingMode.DOWN).longValueExact() : 0L;
        SettlementQuote quote = new SettlementQuote(items, original, original.subtract(payable), payable,
                null, usePoint, givePoint, null);
        if (cmd.addressId() != null) {
            Map<Long, ProductSnapshot> deliverySnapshots = new LinkedHashMap<>(snapshots);
            quote = deliveryService.attach(cmd.memberId(), cmd.addressId(), deliverySnapshots, quote);
        }
        var totalQuantity = lines.stream().mapToInt(PromotionOrderLineCmd::quantity).sum();
        return new PromotionOrderDraft(cmd, type, activityId, recordId, headId, lines.get(0).skuId(),
                totalQuantity, firstPrice, usePoint, quote, drafts);
    }
}

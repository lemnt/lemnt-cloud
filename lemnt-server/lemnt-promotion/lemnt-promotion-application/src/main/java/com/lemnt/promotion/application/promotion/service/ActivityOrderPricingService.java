package com.lemnt.promotion.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.promotion.application.member.service.MemberValidationService;
import com.lemnt.promotion.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.promotion.application.promotion.cmd.PromotionOrderLineCmd;
import com.lemnt.promotion.application.promotion.dto.PromotionOrderDraft;
import com.lemnt.promotion.application.settlement.service.SettlementPromotionSnapshots;
import com.lemnt.promotion.domain.member.gateway.MemberTradeGateway;
import com.lemnt.promotion.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.promotion.domain.product.model.ProductSnapshot;
import com.lemnt.promotion.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.promotion.domain.settlement.model.SettlementItem;
import com.lemnt.promotion.domain.settlement.model.SettlementItemPromotion;
import com.lemnt.promotion.domain.settlement.model.SettlementPromotion;
import com.lemnt.promotion.domain.settlement.model.SettlementQuote;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 活动订单计价：只使用活动价，积分商城支持多商品按行计算积分。 */
@Component
@RequiredArgsConstructor
public class ActivityOrderPricingService {
    private final ProductCatalogGateway productCatalogGateway;
    private final MemberValidationService memberValidationService;
    private final MemberTradeGateway memberTradeGateway;
    private final SeckillStockService seckillStockService;
    private final CombinationRecordService combinationRecordService;
    private final BargainRecordService bargainRecordService;
    private final PointActivityManagementService pointActivityManagementService;
    private final SeckillActivityGateway seckillActivities;
    private final CombinationActivityGateway
            combinationActivities;
    private final BargainActivityGateway bargainActivities;

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
            require(
                    line.quantity() != null && line.quantity() > 0 && line.quantity() <= 999,
                    "购买数量不合法");
        }
        var member = memberValidationService.requireActive(cmd.memberId());
        var skuIds = lines.stream().map(PromotionOrderLineCmd::skuId).distinct().toList();
        require(skuIds.size() == lines.size(), "商品行不能重复");
        var snapshots = productCatalogGateway.findOnShelfBatch(skuIds);
        for (var line : lines) {
            var snapshot = snapshots.get(line.skuId());
            require(
                    snapshot != null
                            && snapshot.stock() != null
                            && snapshot.stock() >= line.quantity(),
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
                    price =
                            seckillStockService
                                    .validateJoin(activityId, line.skuId(), line.quantity())
                                    .seckillPrice();
                }
                case "COMBINATION" -> {
                    require(activityId != null && activityId > 0, "拼团活动编号不能为空");
                    price =
                            combinationRecordService
                                    .validateJoin(
                                            cmd.memberId(),
                                            activityId,
                                            headId,
                                            line.skuId(),
                                            line.quantity())
                                    .combinationPrice();
                }
                case "BARGAIN" -> {
                    require(recordId != null && recordId > 0, "砍价记录编号不能为空");
                    require(line.quantity() == 1, "砍价商品只能购买一件");
                    var join =
                            bargainRecordService.validateJoin(
                                    cmd.memberId(), recordId, line.skuId());
                    activityId = join.activityId();
                    price = join.bargainPrice();
                }
                case "POINT" -> {
                    require(activityId != null && activityId > 0, "积分商城活动编号不能为空");
                    var join =
                            pointActivityManagementService.validateJoin(
                                    activityId, line.skuId(), line.quantity());
                    price = join.price();
                    lineUsePoint = (long) join.point() * line.quantity();
                }
                default -> throw new ServiceException("活动类型不合法", 400);
            }
            var unit = snapshot.price();
            var lineAmount =
                    unit.multiply(BigDecimal.valueOf(line.quantity()))
                            .setScale(2, RoundingMode.DOWN);
            var linePayable =
                    price.multiply(BigDecimal.valueOf(line.quantity()))
                            .setScale(2, RoundingMode.DOWN);
            require(
                    linePayable.signum() >= 0 && linePayable.compareTo(lineAmount) <= 0,
                    "活动价不能为负或高于原价");
            var specs =
                    snapshot.specs() == null
                            ? null
                            : snapshot.specs().stream()
                                    .map(
                                            value ->
                                                    new SettlementItem.Spec(
                                                            value.specId(),
                                                            value.specName(),
                                                            value.specValueId(),
                                                            value.specValueName()))
                                    .toList();
            items.add(
                    new SettlementItem(
                            line.skuId(),
                            snapshot.productCode(),
                            snapshot.name(),
                            unit,
                            line.quantity(),
                            lineAmount,
                            linePayable,
                            BigDecimal.ZERO.setScale(2),
                            snapshot.spuId(),
                            snapshot.spuName(),
                            specs,
                            snapshot.picUrl()));
            drafts.add(
                    new PromotionOrderDraft.Line(
                            line.skuId(), line.quantity(), price, lineUsePoint));
            original = original.add(lineAmount);
            payable = payable.add(linePayable);
            usePoint += lineUsePoint;
            if (index == 0) {
                firstPrice = price;
            }
        }
        require(payable.signum() >= 0 && (payable.signum() > 0 || usePoint > 0), "活动订单金额和积分不能同时为零");
        var memberPoints = member.points() == null ? 0L : member.points();
        require(usePoint <= memberPoints, "会员积分不足");
        var config = memberTradeGateway.config();
        long givePoint =
                config.enabled() && config.givePerYuan() > 0
                        ? payable.multiply(BigDecimal.valueOf(config.givePerYuan()))
                                .setScale(0, RoundingMode.DOWN)
                                .longValueExact()
                        : 0L;
        var children =
                new ArrayList<
                        SettlementItemPromotion>();
        for (int i = 0; i < items.size(); i++) {
            children.add(
                    new SettlementItemPromotion(
                            null,
                            null,
                            null,
                            String.valueOf(i),
                            items.get(i).lineAmount().subtract(items.get(i).payAmount()),
                            BigDecimal.ZERO.setScale(2),
                            drafts.get(i).usePoint(),
                            0L));
        }
        String promotionType = "POINT".equals(type) ? "POINT_EXCHANGE" : type;
        var promotion =
                new SettlementPromotion(
                        null,
                        null,
                        promotionType + ":" + activityId,
                        promotionType,
                        activityId,
                        activityName(type, activityId),
                        "活动价",
                        null,
                        original.subtract(payable),
                        BigDecimal.ZERO.setScale(2),
                        usePoint,
                        0L,
                        Map.of(),
                        children);
        SettlementQuote quote =
                new SettlementQuote(
                        items,
                        original,
                        original.subtract(payable),
                        payable,
                        null,
                        usePoint,
                        givePoint,
                        null,
                        0L,
                        false,
                        Map.of(),
                        List.of(promotion),
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2));
        var fees = new LinkedHashMap<Long, BigDecimal>();
        items.forEach(item -> fees.put(item.productId(), BigDecimal.ZERO.setScale(2)));
        quote =
                SettlementPromotionSnapshots.finish(
                        quote,
                        items,
                        null,
                        fees,
                        givePoint,
                        config.enabled() ? config.givePerYuan() : 0);
        var totalQuantity = lines.stream().mapToInt(PromotionOrderLineCmd::quantity).sum();
        return new PromotionOrderDraft(
                cmd,
                type,
                activityId,
                recordId,
                headId,
                lines.get(0).skuId(),
                totalQuantity,
                firstPrice,
                usePoint,
                quote,
                drafts);
    }

    /** 积分活动没有名称字段，不从备注或当前商品名称伪造活动名。 */
    private String activityName(String type, Long id) {
        return switch (type) {
            case "SECKILL" ->
                    Optional.ofNullable(seckillActivities.find(id))
                            .map(a -> a.name())
                            .orElseThrow(() -> new ServiceException("活动已失效，请重新确认报价", 400));
            case "COMBINATION" ->
                    Optional.ofNullable(combinationActivities.find(id))
                            .map(a -> a.name())
                            .orElseThrow(() -> new ServiceException("活动已失效，请重新确认报价", 400));
            case "BARGAIN" ->
                    Optional.ofNullable(bargainActivities.find(id))
                            .map(a -> a.name())
                            .orElseThrow(() -> new ServiceException("活动已失效，请重新确认报价", 400));
            case "POINT" -> "积分兑换";
            default -> throw new IllegalArgumentException("活动类型不合法");
        };
    }
}

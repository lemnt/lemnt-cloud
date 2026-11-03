package com.lemnt.mall.application.settlement.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.product.model.ProductSnapshot;
import com.lemnt.mall.domain.settlement.gateway.DeliveryPriceGateway;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceLine;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceResult;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** 快递地址和价格来自服务端，结算单冻结收件信息及逐行运费。 */
@Component
@RequiredArgsConstructor
public class SettlementDeliveryService {
    private final MemberDeliveryAddressGateway addresses;
    private final DeliveryPriceGateway prices;
    private final MemberTradeGateway memberTradeGateway;

    /** 输入已完成商品优惠的报价，运费不参与商品优惠门槛。 */
    public SettlementQuote attach(
            Long memberId,
            Long addressId,
            Map<Long, ProductSnapshot> products,
            SettlementQuote quote) {
        var address = addresses.get(memberId, addressId);
        if (address == null
                || !memberId.equals(address.memberId())
                || !addressId.equals(address.id())) {
            throw new ServiceException("收货地址不存在或不属于当前会员", 400);
        }
        if (address.areaId() == null
                || address.areaId() <= 0
                || address.areaId() > Integer.MAX_VALUE
                || address.name() == null
                || address.name().isBlank()
                || address.name().length() > 100
                || address.mobile() == null
                || !address.mobile().matches("[+0-9() -]{5,32}")
                || address.detailAddress() == null
                || address.detailAddress().isBlank()
                || address.detailAddress().length() > 500) {
            throw new ServiceException("收货地址信息不完整", 400);
        }
        var lines =
                quote.items().stream()
                        .map(
                                item -> {
                                    var product = products.get(item.productId());
                                    if (product == null
                                            || product.deliveryTypes() == null
                                            || !product.deliveryTypes().contains(1)
                                            || product.deliveryTemplateId() == null
                                            || product.deliveryTemplateId() <= 0) {
                                        throw new ServiceException("商品未配置快递配送和运费模板", 400);
                                    }
                                    return new DeliveryPriceLine(
                                            item.productId(),
                                            product.deliveryTemplateId(),
                                            item.quantity(),
                                            product.weight(),
                                            product.volume(),
                                            item.payAmount());
                                })
                        .toList();
        var originalFee = prices.price(address.areaId().intValue(), lines);
        var fee =
                quote.freeDelivery()
                        ? new DeliveryPriceResult(
                                BigDecimal.ZERO.setScale(2),
                                quote.items().stream()
                                        .collect(
                                                Collectors.toMap(
                                                        SettlementItem::productId,
                                                        item -> BigDecimal.ZERO.setScale(2))))
                        : originalFee;
        var items =
                quote.items().stream()
                        .map(
                                item ->
                                        new SettlementItem(
                                                item.productId(),
                                                item.productCode(),
                                                item.productName(),
                                                item.unitPrice(),
                                                item.quantity(),
                                                item.lineAmount(),
                                                item.payAmount()
                                                        .add(
                                                                fee.itemPrices()
                                                                        .get(item.productId())),
                                                fee.itemPrices().get(item.productId()),
                                                item.spuId(),
                                                item.spuName(),
                                                item.specs() == null
                                                        ? null
                                                        : item.specs().stream()
                                                                .map(
                                                                        snapshotProperty ->
                                                                                new SettlementItem
                                                                                        .Spec(
                                                                                        snapshotProperty
                                                                                                .specId(),
                                                                                        snapshotProperty
                                                                                                .specName(),
                                                                                        snapshotProperty
                                                                                                .specValueId(),
                                                                                        snapshotProperty
                                                                                                .specValueName()))
                                                                .toList(),
                                                item.picUrl()))
                        .toList();
        var paid = quote.payableAmount().add(fee.total());
        var pointConfig = memberTradeGateway.config();
        long givePoint =
                pointConfig.enabled() && pointConfig.givePerYuan() > 0
                        ? paid.multiply(BigDecimal.valueOf(pointConfig.givePerYuan()))
                                .setScale(0, RoundingMode.DOWN)
                                .longValueExact()
                        : 0L;
        givePoint += quote.rewardPoint();
        return SettlementPromotionSnapshots.finish(
                quote,
                items,
                new SettlementDelivery(
                        address.id(),
                        1,
                        address.name(),
                        address.mobile(),
                        address.areaId(),
                        address.detailAddress(),
                        fee.total(),
                        null),
                originalFee.itemPrices(),
                givePoint - quote.rewardPoint(),
                pointConfig.enabled() ? pointConfig.givePerYuan() : 0);
    }

    /** 自提不计算运费，只冻结门店快照；门店有效性由交易建单再次校验。 */
    public SettlementQuote pickUp(SettlementQuote quote, Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new ServiceException("自提门店不能为空", 400);
        }
        var items =
                quote.items().stream()
                        .map(
                                item ->
                                        new SettlementItem(
                                                item.productId(),
                                                item.productCode(),
                                                item.productName(),
                                                item.unitPrice(),
                                                item.quantity(),
                                                item.lineAmount(),
                                                item.payAmount(),
                                                BigDecimal.ZERO.setScale(2),
                                                item.spuId(),
                                                item.spuName(),
                                                item.specs(),
                                                item.picUrl()))
                        .toList();
        var fees = new LinkedHashMap<Long, BigDecimal>();
        items.forEach(item -> fees.put(item.productId(), BigDecimal.ZERO.setScale(2)));
        var gift =
                quote.promotions().stream()
                        .filter(p -> "ORDER_GIFT".equals(p.promotionType()))
                        .findFirst();
        int rate =
                gift.map(
                                p ->
                                        new BigDecimal(p.ruleSnapshot().replaceAll("[^0-9]", ""))
                                                .intValueExact())
                        .orElse(0);
        return SettlementPromotionSnapshots.finish(
                quote,
                items,
                new SettlementDelivery(
                        null, 2, null, null, null, null, BigDecimal.ZERO.setScale(2), storeId),
                fees,
                quote.givePoint() - quote.rewardPoint(),
                rate);
    }
}

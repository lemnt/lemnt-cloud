package com.lemnt.mall.application.settlement.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.product.model.ProductSnapshot;
import com.lemnt.mall.domain.settlement.gateway.DeliveryPriceGateway;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceLine;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** 快递地址和价格来自服务端，结算单冻结收件信息及逐行运费。 */
@Component
@RequiredArgsConstructor
public class SettlementDeliveryService {
    private final MemberDeliveryAddressGateway addresses;
    private final DeliveryPriceGateway prices;
    private final MemberTradeGateway memberTradeGateway;

    /** 输入已完成商品优惠的报价，运费不参与商品优惠门槛。 */
    public SettlementQuote attach(Long memberId, Long addressId, Map<Long, ProductSnapshot> products,
            SettlementQuote quote
    ) {
        var address = addresses.get(memberId, addressId);
        if (address == null || !memberId.equals(address.memberId()) || !addressId.equals(address.id())
        ) {
            throw new ServiceException("收货地址不存在或不属于当前会员", 400);
        }
        if (address.areaId() == null || address.areaId() <= 0 || address.areaId() > Integer.MAX_VALUE
                || address.name() == null || address.name().isBlank() || address.name().length() > 100
                || address.mobile() == null || !address.mobile().matches("[+0-9() -]{5,32}")
                || address.detailAddress() == null || address.detailAddress().isBlank()
                || address.detailAddress().length() > 500
        ) {
            throw new ServiceException("收货地址信息不完整", 400);
        }
        var lines = quote.items().stream().map(item -> {
            var product = products.get(item.productId());
            if (product == null || product.deliveryTypes() == null || !product.deliveryTypes().contains(1)
                    || product.deliveryTemplateId() == null || product.deliveryTemplateId() <= 0
            ) {
                throw new ServiceException("商品未配置快递配送和运费模板", 400);
            }
            return new DeliveryPriceLine(item.productId(), product.deliveryTemplateId(), item.quantity(),
                    product.weight(), product.volume(), item.payAmount());
        }).toList();
        var fee = prices.price(address.areaId().intValue(), lines);
        var items = quote.items().stream().map(item -> new SettlementItem(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount().add(fee.itemPrices().get(item.productId())),
                fee.itemPrices().get(item.productId()),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new SettlementItem.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl()))
                .toList();
        var paid = quote.payableAmount().add(fee.total());
        var pointConfig = memberTradeGateway.config();
        long givePoint = pointConfig.enabled() && pointConfig.givePerYuan() > 0
                ? paid.multiply(BigDecimal.valueOf(pointConfig.givePerYuan()))
                    .setScale(0, RoundingMode.DOWN).longValueExact() : 0L;
        return new SettlementQuote(items, quote.originalAmount(), quote.discountAmount(),
                quote.payableAmount().add(fee.total()), quote.couponId(), quote.usePoint(), givePoint,
                new SettlementDelivery(address.id(), 1, address.name(), address.mobile(), address.areaId(),
                        address.detailAddress(), fee.total()));
    }
}

package com.lemnt.trade.application.delivery.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.delivery.dto.DeliveryPriceDTO;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceItemQry;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceQry;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateChargeGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateFreeGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 芋道普通快递计价，服务端快照输入，不是允许客户端指定价格的订单接口。 */
@Component
@RequiredArgsConstructor
public class DeliveryPriceService {
    private final TradeConfigGateway config;
    private final DeliveryExpressTemplateGateway templates;
    private final DeliveryExpressTemplateChargeGateway charges;
    private final DeliveryExpressTemplateFreeGateway frees;

    /** 全场/活动包邮优先；同一模板汇总计费后分摊到商品行。 */
    @Transactional(readOnly = true)
    public DeliveryPriceDTO calculate(DeliveryPriceQry qry) {
        require(qry != null && qry.areaId() != null && qry.areaId() > 0
                && qry.items() != null && !qry.items().isEmpty() && qry.items().size() <= 100,
                "运费计算参数不合法");
        Map<Long, Integer> itemPrices = new LinkedHashMap<>();
        Map<Long, List<DeliveryPriceItemQry>> groups = new LinkedHashMap<>();
        long paid = 0;
        for (var item : qry.items()) {
            require(item != null && item.skuId() != null && item.skuId() > 0 && item.templateId() != null
                    && item.templateId() > 0 && item.count() != null && item.count() > 0 && item.count() <= 999
                    && item.payPrice() != null && item.payPrice() >= 0, "运费商品快照不合法");
            require(itemPrices.put(item.skuId(), 0) == null, "运费商品不能重复");
            groups.computeIfAbsent(item.templateId(), key -> new ArrayList<>()).add(item);
            paid += item.payPrice();
        }
        require(paid <= Integer.MAX_VALUE, "商品实付金额超出计价范围");
        var settings = config.get();
        require(settings != null, "交易配置缺失");
        if (Boolean.TRUE.equals(qry.freeDelivery()) || (Boolean.TRUE.equals(settings.deliveryExpressFreeEnabled())
                && settings.deliveryExpressFreePrice() != null && paid >= settings.deliveryExpressFreePrice())) {
            return new DeliveryPriceDTO(0, Map.copyOf(itemPrices));
        }
        long total = 0;
        for (var entry : groups.entrySet()) {
            var template = templates.find(entry.getKey());
            require(template != null, "商品运费模板不存在");
            int mode = template.chargeMode();
            var items = entry.getValue();
            BigDecimal quantity = items.stream().map(item -> chargeValue(item, mode))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long groupPaid = items.stream().mapToLong(DeliveryPriceItemQry::payPrice).sum();
            var free = frees.byTemplate(template.id()).stream()
                    .filter(rule -> rule.areaIds().contains(qry.areaId())).findFirst().orElse(null);
            if (free != null && quantity.compareTo(BigDecimal.valueOf(free.freeCount())) >= 0
                    && groupPaid >= free.freePrice()) {
                continue;
            }
            var rule = charges.byTemplate(template.id()).stream()
                    .filter(value -> value.areaIds().contains(qry.areaId())).findFirst().orElse(null);
            require(rule != null, "收件地区未配置运费，不能下单");
            require(rule.chargeMode() == mode && rule.startCount().signum() > 0
                    && rule.extraCount().signum() > 0 && rule.startPrice() >= 0 && rule.extraPrice() >= 0,
                    "运费计费配置不合法");
            BigDecimal excess = quantity.subtract(rule.startCount()).max(BigDecimal.ZERO);
            BigDecimal price = excess.divide(rule.extraCount(), 0, RoundingMode.CEILING)
                    .multiply(BigDecimal.valueOf(rule.extraPrice())).add(BigDecimal.valueOf(rule.startPrice()));
            require(price.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0, "运费超出计价范围");
            int fee = price.intValueExact();
            int remaining = fee;
            for (int i = 0; i < items.size(); i++) {
                var item = items.get(i);
                int part = i == items.size() - 1 ? remaining : BigDecimal.valueOf(fee)
                        .multiply(chargeValue(item, mode)).divide(quantity, 0, RoundingMode.DOWN).intValueExact();
                itemPrices.put(item.skuId(), part);
                remaining -= part;
            }
            total += fee;
            require(total <= Integer.MAX_VALUE, "总运费超出计价范围");
        }
        return new DeliveryPriceDTO((int) total, Map.copyOf(itemPrices));
    }

    private BigDecimal chargeValue(DeliveryPriceItemQry item, int mode) {
        if (mode == 1) {
            return BigDecimal.valueOf(item.count());
        }
        require(mode == 2 || mode == 3, "运费计费方式不合法");
        var value = mode == 2 ? item.weight() : item.volume();
        require(value != null && value.signum() > 0, "商品缺少有效重量或体积，不能计算运费");
        return value.multiply(BigDecimal.valueOf(item.count()));
    }

    private void require(boolean valid, String message) {
        if (!valid) {
            throw new ServiceException(message, 400);
        }
    }
}

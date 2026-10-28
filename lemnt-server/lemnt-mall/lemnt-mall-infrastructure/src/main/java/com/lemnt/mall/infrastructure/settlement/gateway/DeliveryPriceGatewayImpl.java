package com.lemnt.mall.infrastructure.settlement.gateway;

import com.lemnt.mall.domain.settlement.gateway.DeliveryPriceGateway;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceLine;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceResult;
import com.lemnt.trade.client.TradeDeliveryClient;
import com.lemnt.trade.client.bo.DeliveryFeeBo;
import com.lemnt.trade.client.bo.DeliveryFeeItemBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

/** 在网关边界转换元/分，拒绝不完整或不守恒的远程运费结果。 */
@Component
public class DeliveryPriceGatewayImpl implements DeliveryPriceGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private TradeDeliveryClient client;

    @Override
    public DeliveryPriceResult price(Integer areaId, List<DeliveryPriceLine> items) {
        var result = client.price(new DeliveryFeeBo(areaId, false, items.stream().map(row ->
                new DeliveryFeeItemBo(row.skuId(), row.templateId(), row.count(), row.weight(), row.volume(),
                        fen(row.payAmount()))).toList()));
        if (result == null || result.deliveryPrice() == null || result.deliveryPrice() < 0
                || result.itemPrices() == null || result.itemPrices().size() != items.size()
                || items.stream().anyMatch(row -> !result.itemPrices().containsKey(row.skuId()))
                || result.itemPrices().values().stream().anyMatch(value -> value == null || value < 0)
                || result.itemPrices().values().stream().mapToLong(Integer::longValue).sum() != result.deliveryPrice()
        ) {
            throw new IllegalStateException("运费结果不完整或金额不一致");
        }
        var allocations = new LinkedHashMap<Long, BigDecimal>();
        result.itemPrices().forEach((id, value) -> allocations.put(id, BigDecimal.valueOf(value, 2)));
        return new DeliveryPriceResult(BigDecimal.valueOf(result.deliveryPrice(), 2), allocations);
    }

    /** 参考计价契约为整数分，超出范围明确拒绝，不能发生截断或溢出。 */
    private int fen(BigDecimal value) {
        try {
            return value.movePointRight(2).intValueExact();
        } catch (ArithmeticException exception) {
            throw new com.lemnt.common.core.exception.ServiceException("商品金额超出运费计价范围", 400);
        }
    }
}

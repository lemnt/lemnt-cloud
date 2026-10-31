package com.lemnt.trade.application.dubbo;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceItemQry;
import com.lemnt.trade.application.delivery.qry.DeliveryPriceQry;
import com.lemnt.trade.application.delivery.service.DeliveryPriceService;
import com.lemnt.trade.client.TradeDeliveryClient;
import com.lemnt.trade.client.bo.DeliveryFeeBo;
import com.lemnt.trade.client.dto.DeliveryFeeDTO;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/** 跨服务输入转换为运费应用查询，不与商城共享数据库事务。 */
@DubboService
@RequiredArgsConstructor
public class TradeDeliveryClientImpl implements TradeDeliveryClient {
    private final DeliveryPriceService prices;

    @Override
    public DeliveryFeeDTO price(DeliveryFeeBo input) {
        if (input == null || input.items() == null || input.items().stream().anyMatch(Objects::isNull)
        ) {
            throw new ServiceException("运费快照不合法", 400);
        }
        var value = prices.calculate(new DeliveryPriceQry(input.areaId(), input.freeDelivery(),
                input.items().stream().map(item -> new DeliveryPriceItemQry(item.skuId(), item.templateId(),
                        item.count(), item.weight(), item.volume(), item.payPrice())).toList()));
        return new DeliveryFeeDTO(value.deliveryPrice(), value.itemPrices());
    }
}

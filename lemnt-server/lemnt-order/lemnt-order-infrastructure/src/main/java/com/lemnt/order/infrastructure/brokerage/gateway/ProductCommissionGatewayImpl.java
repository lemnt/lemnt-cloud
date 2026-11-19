package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.order.domain.brokerage.gateway.ProductCommissionGateway;
import com.lemnt.order.domain.brokerage.model.BrokeragePrice;
import com.lemnt.order.domain.brokerage.model.FixedCommission;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductFixedCommissionDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 订单域只通过商品客户端读取当前固定佣金。 */
@Component
public class ProductCommissionGatewayImpl implements ProductCommissionGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCatalogClient products;

    @Override
    public Map<Long, FixedCommission> fixedCommissions(List<Long> skuIds) {
        return products.fixedCommissions(skuIds).stream()
                .collect(
                        Collectors.toMap(
                                ProductFixedCommissionDTO::skuId,
                                value -> new FixedCommission(value.first(), value.second())));
    }

    @Override
    public List<BrokeragePrice> brokeragePrices(Long spuId) {
        return products.brokeragePrices(spuId).stream()
                .map(value -> new BrokeragePrice(value.price(), value.firstFixed()))
                .toList();
    }
}

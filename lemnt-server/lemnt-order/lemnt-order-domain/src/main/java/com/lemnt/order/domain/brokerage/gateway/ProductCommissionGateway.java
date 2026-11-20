package com.lemnt.order.domain.brokerage.gateway;

import com.lemnt.order.domain.brokerage.model.BrokeragePrice;
import com.lemnt.order.domain.brokerage.model.FixedCommission;

import java.util.List;
import java.util.Map;

/** 建单时读取商品配置，历史退款使用订单自己的快照。 */
public interface ProductCommissionGateway {
    Map<Long, FixedCommission> fixedCommissions(List<Long> skuIds);

    /** 读取在售 SKU 的单件佣金预估基础。 */
    List<BrokeragePrice> brokeragePrices(Long spuId);
}

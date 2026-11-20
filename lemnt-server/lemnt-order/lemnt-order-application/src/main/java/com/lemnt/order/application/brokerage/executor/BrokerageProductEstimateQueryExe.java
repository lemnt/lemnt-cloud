package com.lemnt.order.application.brokerage.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.brokerage.dto.BrokerageProductEstimateDTO;
import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.application.brokerage.service.OrderCommissionService;
import com.lemnt.order.domain.brokerage.gateway.BrokerageUserGateway;
import com.lemnt.order.domain.brokerage.gateway.ProductCommissionGateway;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;

/** 只读预估，按当前分销资格与在售 SKU 标价或每件固定额计算。 */
@Component
@RequiredArgsConstructor
public class BrokerageProductEstimateQueryExe {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BrokerageProductEstimateDTO UNAVAILABLE =
            new BrokerageProductEstimateDTO(false, ZERO, ZERO);

    private final TradeConfigGateway configs;
    private final BrokerageUserGateway users;
    private final ProductCommissionGateway products;

    /** 预估不创建分销账户，人人分销时按现有开户规则判断新会员。 */
    public BrokerageProductEstimateDTO execute(Long memberId, Long spuId) {
        if (memberId == null || memberId <= 0) {
            throw new ServiceException("请先登录", 401);
        }
        if (spuId == null || spuId <= 0) {
            throw new IllegalArgumentException("商品编号不合法");
        }
        var config = configs.get();
        if (config == null || !Boolean.TRUE.equals(config.brokerageEnabled())) {
            return UNAVAILABLE;
        }
        var user = users.find(memberId);
        boolean eligible =
                user == null
                        ? Integer.valueOf(BrokerageService.ENABLED_ALL)
                                .equals(config.brokerageEnabledCondition())
                        : Boolean.TRUE.equals(user.brokerageEnabled());
        if (!eligible) {
            return UNAVAILABLE;
        }
        var amounts =
                products.brokeragePrices(spuId).stream()
                        .map(
                                price ->
                                        price.firstFixed() != null
                                                ? price.firstFixed()
                                                : OrderCommissionService.percentage(
                                                        price.price(),
                                                        config.brokerageFirstPercent()))
                        .toList();
        return amounts.isEmpty()
                ? UNAVAILABLE
                : new BrokerageProductEstimateDTO(
                        true,
                        amounts.stream().min(Comparator.naturalOrder()).orElseThrow(),
                        amounts.stream().max(Comparator.naturalOrder()).orElseThrow());
    }
}

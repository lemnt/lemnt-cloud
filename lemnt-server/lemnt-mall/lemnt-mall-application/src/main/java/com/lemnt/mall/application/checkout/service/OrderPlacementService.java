package com.lemnt.mall.application.checkout.service;

import com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd;
import com.lemnt.mall.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.mall.domain.checkout.gateway.SettlementGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutQuote;
import com.lemnt.mall.domain.checkout.model.Settlement;
import com.lemnt.mall.domain.order.gateway.OrderGateway;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 先提交商城本地准备事务，再把服务端缓存快照提交到交易服务。 */
@Component
@RequiredArgsConstructor
public class OrderPlacementService {
    private final CheckoutPreparationService preparation;
    private final OrderGateway orders;

    /** 购物车提单，远程失败保留原请求及快照以供重试。 */
    public OrderSnapshot checkoutCart(CheckoutSubmitCmd cmd) {
        return orders.save(preparation.checkoutCart(cmd));
    }

    /** 直接购买提单。 */
    public OrderSnapshot createDirect(CheckoutDirectCmd cmd) {
        return orders.save(preparation.createDirect(cmd));
    }

    /** 预览服务端报价。 */
    public CheckoutQuote quote(CheckoutQuoteQry query) {
        return preparation.quote(query);
    }

    /** 创建仅保存在 Redis 的结算单。 */
    public Settlement prepareSettlement(CheckoutQuoteQry query) {
        return preparation.prepareSettlement(query);
    }

    /** 读取并校验 Redis 结算单后创建交易订单。 */
    public OrderSnapshot submitSettlement(Long memberId, String id, String requestNo, SettlementGateway cache) {
        return orders.save(preparation.submitSettlement(memberId, id, requestNo, cache));
    }

    /** 放弃尚未提交的结算；已提交的订单通过交易服务查询。 */
    public OrderSnapshot abandonSettlement(Long memberId, String id, String requestNo, SettlementGateway cache) {
        var order = preparation.abandonSettlement(memberId, id, requestNo, cache);
        return order == null ? null : orders.save(order);
    }
}

package com.lemnt.mall.application.settlement.service;

import com.lemnt.mall.application.settlement.cmd.SettlementDirectCmd;
import com.lemnt.mall.application.settlement.cmd.SettlementOrderSubmitCmd;
import com.lemnt.mall.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 先提交商城本地准备事务，再把服务端缓存快照提交到交易服务。 */
@Component
@RequiredArgsConstructor
public class OrderPlacementService {
    private final SettlementPreparationService preparation;
    private final OrderGateway orders;

    /** 购物车提单，远程失败保留原请求及快照以供重试。 */
    public SettlementOrder checkoutCart(SettlementOrderSubmitCmd cmd) {
        return orders.save(preparation.checkoutCart(cmd));
    }

    /** 直接购买提单。 */
    public SettlementOrder createDirect(SettlementDirectCmd cmd) {
        return orders.save(preparation.createDirect(cmd));
    }

    /** 预览服务端报价。 */
    public SettlementQuote quote(SettlementQuoteQry query) {
        return preparation.quote(query);
    }

    /** 创建仅保存在 Redis 的结算单。 */
    public Settlement prepareSettlement(SettlementQuoteQry query) {
        return preparation.prepareSettlement(query);
    }

    /** 读取并校验 Redis 结算单后创建交易订单。 */
    public SettlementOrder submitSettlement(Long memberId, String id, String requestNo, SettlementGateway cache) {
        return orders.save(preparation.submitSettlement(memberId, id, requestNo, cache));
    }

    /** 放弃尚未提交的结算；已提交的订单通过交易服务查询。 */
    public SettlementOrder abandonSettlement(Long memberId, String id, String requestNo, SettlementGateway cache) {
        var order = preparation.abandonSettlement(memberId, id, requestNo, cache);
        return order == null ? null : orders.save(order);
    }
}

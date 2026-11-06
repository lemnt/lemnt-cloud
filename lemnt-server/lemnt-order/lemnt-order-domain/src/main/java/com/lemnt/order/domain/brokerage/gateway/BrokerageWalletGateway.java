package com.lemnt.order.domain.brokerage.gateway;

import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;

/**
 * 钱包在支付库独立提交，原审批号保证网络重试不重复入账。
 */
public interface BrokerageWalletGateway {
    void credit(BrokerageWithdraw approved);
}

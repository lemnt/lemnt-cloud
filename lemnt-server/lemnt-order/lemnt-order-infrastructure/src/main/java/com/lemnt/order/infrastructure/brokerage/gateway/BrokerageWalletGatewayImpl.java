package com.lemnt.order.infrastructure.brokerage.gateway;

import com.lemnt.order.domain.brokerage.gateway.BrokerageWalletGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.pay.client.WalletClient;
import com.lemnt.pay.client.bo.BrokerageWithdrawalBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 禁止在订单本地事务中把钱包远程成功当成可回滚写入。
 */
@Component
public class BrokerageWalletGatewayImpl implements BrokerageWalletGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private WalletClient client;

    public void credit(BrokerageWithdraw v) {
        client.creditBrokerageWithdrawal(
            new BrokerageWithdrawalBo(v.id(), v.userId(), v.price().subtract(v.feePrice())));
    }
}

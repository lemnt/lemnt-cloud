package com.lemnt.order.application.brokerage.executor;

import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawCreateCmd;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 佣金提现申请执行器。 */
@Component
@RequiredArgsConstructor
public class BrokerageWithdrawCreateCmdExe {
    private final BrokerageWithdrawService service;

    public Long execute(BrokerageWithdrawCreateCmd cmd) {
        return service.create(cmd);
    }
}

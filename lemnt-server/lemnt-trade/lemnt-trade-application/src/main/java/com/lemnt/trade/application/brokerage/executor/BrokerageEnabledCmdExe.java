package com.lemnt.trade.application.brokerage.executor;

import com.lemnt.trade.application.brokerage.cmd.BrokerageEnabledCmd;
import com.lemnt.trade.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 分销资格开关执行器。 */
@Component
@RequiredArgsConstructor
public class BrokerageEnabledCmdExe {
    private final BrokerageService service;

    public Boolean execute(BrokerageEnabledCmd cmd) {
        return service.setEnabled(cmd.memberId(), cmd.enabled());
    }
}

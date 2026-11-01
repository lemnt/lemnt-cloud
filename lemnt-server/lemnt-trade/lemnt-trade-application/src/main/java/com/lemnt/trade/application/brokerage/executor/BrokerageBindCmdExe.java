package com.lemnt.trade.application.brokerage.executor;

import com.lemnt.trade.application.brokerage.cmd.BrokerageBindCmd;
import com.lemnt.trade.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员绑定推广员执行器。 */
@Component
@RequiredArgsConstructor
public class BrokerageBindCmdExe {
    private final BrokerageService service;

    public Boolean execute(BrokerageBindCmd cmd) {
        return service.bind(cmd.memberId(), cmd.bindUserId());
    }
}

package com.lemnt.order.application.brokerage.executor;

import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 手动触发到期佣金解冻执行器。 */
@Component
@RequiredArgsConstructor
public class BrokerageUnfreezeCmdExe {
    private final BrokerageService service;

    public int execute(int limit) {
        return service.unfreezeDue(limit);
    }
}

package com.lemnt.order.application.brokerage.executor;

import com.lemnt.order.application.brokerage.cmd.BrokerageAdminBindCmd;
import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 管理端修改推广员执行器。
 */
@Component
@RequiredArgsConstructor
public class BrokerageAdminBindCmdExe {
    private final BrokerageService service;

    public Boolean execute(BrokerageAdminBindCmd cmd) {
        return service.adminBind(cmd.memberId(), cmd.bindUserId());
    }
}

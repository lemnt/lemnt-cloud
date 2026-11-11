package com.lemnt.order.application.brokerage.executor;

import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 提现审核执行器。
 */
@Component
@RequiredArgsConstructor
public class BrokerageWithdrawAuditCmdExe {
    private final BrokerageWithdrawService service;

    public Boolean execute(BrokerageWithdrawAuditCmd cmd) {
        return service.audit(cmd);
    }
}

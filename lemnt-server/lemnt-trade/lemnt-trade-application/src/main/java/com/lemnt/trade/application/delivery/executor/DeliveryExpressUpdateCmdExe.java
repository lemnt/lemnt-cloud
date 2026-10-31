package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.cmd.DeliveryExpressUpdateCmd;
import com.lemnt.trade.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送修改执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressUpdateCmdExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public boolean execute(DeliveryExpressUpdateCmd cmd) {
        return service.updateExpress(cmd);
    }
}

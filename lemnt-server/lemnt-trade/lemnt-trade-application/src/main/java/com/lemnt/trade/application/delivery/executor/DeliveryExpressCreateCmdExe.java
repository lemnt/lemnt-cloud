package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.cmd.DeliveryExpressCreateCmd;
import com.lemnt.trade.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送新增执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressCreateCmdExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public Long execute(DeliveryExpressCreateCmd cmd) {
        return service.createExpress(cmd);
    }
}

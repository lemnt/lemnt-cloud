package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.trade.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送新增执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateCreateCmdExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public Long execute(DeliveryExpressTemplateCreateCmd cmd) {
        return service.createTemplate(cmd);
    }
}

package com.lemnt.order.application.delivery.executor;

import com.lemnt.order.application.delivery.cmd.DeliveryExpressTemplateUpdateCmd;
import com.lemnt.order.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送修改执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateUpdateCmdExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public boolean execute(DeliveryExpressTemplateUpdateCmd cmd) {
        return service.updateTemplate(cmd);
    }
}

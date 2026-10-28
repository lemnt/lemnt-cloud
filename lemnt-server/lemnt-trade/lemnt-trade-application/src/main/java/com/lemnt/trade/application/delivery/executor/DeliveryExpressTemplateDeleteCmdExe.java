package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送删除执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateDeleteCmdExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public boolean execute(Long id) {
        return service.deleteTemplate(id);
    }
}

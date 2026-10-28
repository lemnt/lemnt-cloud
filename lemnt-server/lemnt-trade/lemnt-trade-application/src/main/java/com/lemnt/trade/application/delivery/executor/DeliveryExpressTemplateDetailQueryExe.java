package com.lemnt.trade.application.delivery.executor;

import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateDetailDTO;
import com.lemnt.trade.application.delivery.service.DeliveryManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配送详情执行器。 */
@Component
@RequiredArgsConstructor
public class DeliveryExpressTemplateDetailQueryExe {
    private final DeliveryManagementService service;

    /** 执行配送操作。 */
    public DeliveryExpressTemplateDetailDTO execute(Long id) {
        return service.detail(id);
    }
}

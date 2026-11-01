package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.trade.application.pickup.service.PickUpOrderService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 核销码预览执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpPreviewQueryExe {
    private final PickUpOrderService service;

    public PickUpOrderDTO execute(String verifyCode) {
        return service.preview(verifyCode);
    }
}

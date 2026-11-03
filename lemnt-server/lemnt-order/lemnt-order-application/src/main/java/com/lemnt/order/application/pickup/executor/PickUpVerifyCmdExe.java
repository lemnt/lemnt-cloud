package com.lemnt.order.application.pickup.executor;

import com.lemnt.order.application.pickup.cmd.PickUpVerifyCmd;
import com.lemnt.order.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.order.application.pickup.service.PickUpOrderService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 自提核销执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpVerifyCmdExe {
    private final PickUpOrderService service;

    public PickUpOrderDTO execute(PickUpVerifyCmd cmd) {
        return service.verify(cmd);
    }
}

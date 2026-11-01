package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.cmd.PickUpVerifyCmd;
import com.lemnt.trade.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.trade.application.pickup.service.PickUpOrderService;

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

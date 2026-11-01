package com.lemnt.trade.application.pickup.executor;

import com.lemnt.trade.application.pickup.cmd.PickUpAssignCmd;
import com.lemnt.trade.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.trade.application.pickup.service.PickUpOrderService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 设置自提订单执行器。 */
@Component
@RequiredArgsConstructor
public class PickUpAssignCmdExe {
    private final PickUpOrderService service;

    public PickUpOrderDTO execute(PickUpAssignCmd cmd) {
        return service.assign(cmd);
    }
}

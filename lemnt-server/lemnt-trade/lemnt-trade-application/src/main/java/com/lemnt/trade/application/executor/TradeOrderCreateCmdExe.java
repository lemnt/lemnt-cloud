package com.lemnt.trade.application.executor;

import com.lemnt.trade.application.cmd.TradeOrderCreateCmd;
import com.lemnt.trade.application.checkout.service.OrderPlacementService;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeOrderCreateCmdExe {
    private final OrderPlacementService placement;

    public TradeOrder execute(TradeOrderCreateCmd cmd) {
        return placement.createDirect(cmd);
    }
}

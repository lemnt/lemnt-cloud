package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.cmd.PromotionStateChangeCmd;
import com.lemnt.trade.application.promotion.dto.PromotionResultDTO;
import com.lemnt.trade.application.promotion.service.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionStateChangeCmdExe {
    private final PromotionAdministrationService service;

    public PromotionResultDTO execute(PromotionStateChangeCmd c) {
        return PromotionDtoMapper.result(
                service.changeState(
                        c.operatorId(), c.requestNo(), c.id(), c.expectedRevision(), c.target()));
    }
}

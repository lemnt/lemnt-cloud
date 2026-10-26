package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.cmd.PromotionPublishCmd;
import com.lemnt.trade.application.promotion.dto.PromotionResultDTO;
import com.lemnt.trade.application.promotion.service.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionPublishCmdExe {
    private final PromotionAdministrationService service;

    public PromotionResultDTO execute(PromotionPublishCmd c) {
        return PromotionDtoMapper.result(
                service.publish(c.operatorId(), c.requestNo(), c.id(), c.expectedRevision()));
    }
}

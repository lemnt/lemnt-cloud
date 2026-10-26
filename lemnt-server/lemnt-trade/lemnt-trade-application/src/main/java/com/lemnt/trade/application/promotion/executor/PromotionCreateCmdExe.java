package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.cmd.PromotionCreateCmd;
import com.lemnt.trade.application.promotion.dto.PromotionResultDTO;
import com.lemnt.trade.application.promotion.service.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionCreateCmdExe {
    private final PromotionAdministrationService service;

    public PromotionResultDTO execute(PromotionCreateCmd c) {
        return PromotionDtoMapper.result(
                service.create(c.operatorId(), c.requestNo(), c.code(), c.draft()));
    }
}

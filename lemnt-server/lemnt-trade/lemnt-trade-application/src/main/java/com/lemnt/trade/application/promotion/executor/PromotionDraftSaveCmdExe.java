package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.cmd.PromotionDraftSaveCmd;
import com.lemnt.trade.application.promotion.dto.PromotionResultDTO;
import com.lemnt.trade.application.promotion.service.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionDraftSaveCmdExe {
    private final PromotionAdministrationService service;

    public PromotionResultDTO execute(PromotionDraftSaveCmd c) {
        return PromotionDtoMapper.result(
                service.saveDraft(
                        c.operatorId(), c.requestNo(), c.id(), c.expectedRevision(), c.draft()));
    }
}

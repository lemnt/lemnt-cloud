package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PromotionDraftSaveCmd;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.application.promotion.service.PromotionManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销草稿保存命令执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionDraftSaveCmdExe {
    private final PromotionManagementService promotionAdministrationService;

    /**
     * 执行营销草稿保存命令。
     */
    public PromotionResultDTO execute(PromotionDraftSaveCmd c) {
        return PromotionDtoMapper.result(
                promotionAdministrationService.saveDraft(
                c.operatorId(), c.requestNo(), c.id(), c.expectedRevision(), c.draft()));
    }
}

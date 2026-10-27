package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PromotionPublishCmd;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.application.promotion.service.PromotionManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销发布命令执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionPublishCmdExe {
    private final PromotionManagementService promotionAdministrationService;

    /**
     * 执行营销发布命令。
     */
    public PromotionResultDTO execute(PromotionPublishCmd c) {
        return PromotionDtoMapper.result(
                promotionAdministrationService.publish(c.operatorId(), c.requestNo(), c.id(), c.expectedRevision()));
    }
}

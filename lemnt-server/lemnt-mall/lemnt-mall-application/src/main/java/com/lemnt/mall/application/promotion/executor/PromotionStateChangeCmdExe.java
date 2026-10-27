package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PromotionStateChangeCmd;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.application.promotion.service.PromotionManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销状态修改命令执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionStateChangeCmdExe {
    private final PromotionManagementService promotionAdministrationService;

    /**
     * 执行营销状态修改命令。
     */
    public PromotionResultDTO execute(PromotionStateChangeCmd c) {
        return PromotionDtoMapper.result(
                promotionAdministrationService.changeState(
                c.operatorId(), c.requestNo(), c.id(), c.expectedRevision(), c.target()));
    }
}

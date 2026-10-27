package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.cmd.PromotionCreateCmd;
import com.lemnt.mall.application.promotion.dto.PromotionResultDTO;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.application.promotion.service.PromotionManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionCreateCmdExe {
    private final PromotionManagementService promotionAdministrationService;

    /**
     * 执行营销创建命令。
     */
    public PromotionResultDTO execute(PromotionCreateCmd c) {
        return PromotionDtoMapper.result(
                promotionAdministrationService.create(c.operatorId(), c.requestNo(), c.code(), c.draft()));
    }
}

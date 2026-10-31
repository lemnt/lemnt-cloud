package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.BargainHelpDTO;
import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 好友助力执行器。 */
@Component
@RequiredArgsConstructor
public class BargainHelpCreateCmdExe {
    private final BargainRecordService service;

    public BargainHelpDTO execute(Long userId, Long recordId) {
        return service.help(userId, recordId);
    }
}

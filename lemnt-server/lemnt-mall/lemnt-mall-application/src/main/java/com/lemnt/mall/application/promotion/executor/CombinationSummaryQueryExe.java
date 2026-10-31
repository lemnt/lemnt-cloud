package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.CombinationSummaryDTO;
import com.lemnt.mall.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团概要执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationSummaryQueryExe {
    private final CombinationRecordService service;

    public CombinationSummaryDTO execute() {
        return service.summary();
    }
}

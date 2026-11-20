package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.CombinationSummaryDTO;
import com.lemnt.promotion.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 拼团概要执行器。
 */
@Component
@RequiredArgsConstructor
public class CombinationSummaryQueryExe {
    private final CombinationRecordService service;

    public CombinationSummaryDTO execute() {
        return service.summary();
    }
}

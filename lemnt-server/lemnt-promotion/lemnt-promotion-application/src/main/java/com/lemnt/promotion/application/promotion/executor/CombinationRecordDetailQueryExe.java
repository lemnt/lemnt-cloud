package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.dto.CombinationRecordDetailDTO;
import com.lemnt.promotion.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团记录详情执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationRecordDetailQueryExe {
    private final CombinationRecordService service;

    public CombinationRecordDetailDTO execute(Long id, Long userId) {
        return service.detail(id, userId);
    }
}

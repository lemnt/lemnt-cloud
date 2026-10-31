package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.BargainDetailDTO;
import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价详情执行器。 */
@Component
@RequiredArgsConstructor
public class BargainDetailQueryExe {
    private final BargainRecordService service;

    public BargainDetailDTO execute(Long recordId, Long userId) {
        return service.detail(recordId, userId);
    }
}

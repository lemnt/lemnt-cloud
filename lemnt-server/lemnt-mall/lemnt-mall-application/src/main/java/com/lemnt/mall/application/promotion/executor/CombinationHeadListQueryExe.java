package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.CombinationRecordDTO;
import com.lemnt.mall.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 活动可参与团长记录执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationHeadListQueryExe {
    private final CombinationRecordService service;

    public List<CombinationRecordDTO> execute(Long activityId, Integer status, int count) {
        return service.heads(activityId, status, count);
    }
}

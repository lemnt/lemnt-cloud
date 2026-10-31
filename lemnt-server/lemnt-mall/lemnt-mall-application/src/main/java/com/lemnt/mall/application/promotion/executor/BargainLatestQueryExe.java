package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.BargainRecordDTO;
import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 最近砍价记录执行器。 */
@Component
@RequiredArgsConstructor
public class BargainLatestQueryExe {
    private final BargainRecordService service;

    public List<BargainRecordDTO> execute(Long activityId, Integer status, int count) {
        return service.latest(activityId, status, count);
    }
}

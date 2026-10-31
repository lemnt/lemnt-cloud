package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDTO;
import com.lemnt.mall.application.promotion.qry.CombinationRecordPageQry;
import com.lemnt.mall.application.promotion.service.CombinationRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 拼团记录分页执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationRecordPageQueryExe {
    private final CombinationRecordService service;

    public PageResult<CombinationRecordDTO> execute(CombinationRecordPageQry qry) {
        return service.page(qry);
    }
}

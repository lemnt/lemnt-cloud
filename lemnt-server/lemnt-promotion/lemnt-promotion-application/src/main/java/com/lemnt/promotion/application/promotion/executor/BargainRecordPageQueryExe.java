package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.promotion.dto.BargainRecordDTO;
import com.lemnt.promotion.application.promotion.qry.BargainRecordPageQry;
import com.lemnt.promotion.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价记录分页执行器。 */
@Component
@RequiredArgsConstructor
public class BargainRecordPageQueryExe {
    private final BargainRecordService service;

    public PageResult<BargainRecordDTO> execute(BargainRecordPageQry qry) {
        return service.page(qry);
    }
}

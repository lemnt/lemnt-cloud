package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.BargainHelpDTO;
import com.lemnt.mall.application.promotion.qry.BargainHelpPageQry;
import com.lemnt.mall.application.promotion.service.BargainRecordService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 砍价助力分页执行器。 */
@Component
@RequiredArgsConstructor
public class BargainHelpPageQueryExe {
    private final BargainRecordService service;

    public PageResult<BargainHelpDTO> execute(BargainHelpPageQry qry) {
        return service.helpPage(qry);
    }
}

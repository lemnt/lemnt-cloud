package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.DiyPageDTO;
import com.lemnt.mall.application.promotion.qry.DiyPagePageQry;
import com.lemnt.mall.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面分页执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPagePageQueryExe {
    private final DiyDecorationService service;

    public PageResult<DiyPageDTO> execute(DiyPagePageQry qry) {
        return service.pagePage(qry);
    }
}

package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修页面分页执行器。 */
@Component
@RequiredArgsConstructor
public class DiyPagePageQueryExe {
    private final DiyDecorationService service;

    public com.lemnt.common.domain.model.PageResult<
                    com.lemnt.promotion.application.promotion.dto.DiyPageDTO>
            execute(com.lemnt.promotion.application.promotion.qry.DiyPagePageQry qry) {
        return service.pagePage(qry);
    }
}

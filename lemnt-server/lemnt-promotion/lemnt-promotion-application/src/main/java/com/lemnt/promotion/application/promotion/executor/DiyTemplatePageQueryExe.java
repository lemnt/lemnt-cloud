package com.lemnt.promotion.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.promotion.application.promotion.dto.DiyTemplateDTO;
import com.lemnt.promotion.application.promotion.qry.DiyTemplatePageQry;
import com.lemnt.promotion.application.promotion.service.DiyDecorationService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 装修模板分页执行器。 */
@Component
@RequiredArgsConstructor
public class DiyTemplatePageQueryExe {
    private final DiyDecorationService service;

    public PageResult<
                    DiyTemplateDTO>
            execute(DiyTemplatePageQry qry) {
        return service.templatePage(qry);
    }
}

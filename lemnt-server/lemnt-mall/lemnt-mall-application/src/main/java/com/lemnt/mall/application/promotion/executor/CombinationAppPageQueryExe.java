package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.CombinationAppActivityDTO;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 拼团活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class CombinationAppPageQueryExe {
    private final CombinationActivityManagementService service;

    public PageResult<CombinationAppActivityDTO> execute(int pageNum, int pageSize) {
        return service.appPage(pageNum, pageSize);
    }
}

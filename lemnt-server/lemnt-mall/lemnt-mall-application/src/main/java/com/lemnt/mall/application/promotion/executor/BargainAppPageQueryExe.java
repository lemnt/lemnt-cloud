package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** App 砍价活动分页执行器。 */
@Component
@RequiredArgsConstructor
public class BargainAppPageQueryExe {
    private final BargainActivityManagementService service;

    public PageResult<BargainActivityDTO> execute(int pageNum, int pageSize) {
        return service.appPage(pageNum, pageSize);
    }
}

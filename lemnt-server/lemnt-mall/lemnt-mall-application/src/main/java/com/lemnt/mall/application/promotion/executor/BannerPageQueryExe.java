package com.lemnt.mall.application.promotion.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.BannerDTO;
import com.lemnt.mall.application.promotion.qry.BannerPageQry;
import com.lemnt.mall.application.promotion.service.BannerManagementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Banner 分页执行器。 */
@Component
@RequiredArgsConstructor
public class BannerPageQueryExe {
    private final BannerManagementService service;

    public PageResult<BannerDTO> execute(BannerPageQry qry) {
        return service.page(qry);
    }
}

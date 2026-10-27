package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.PromotionPageDTO;
import com.lemnt.mall.application.promotion.qry.PromotionPageQry;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.model.PromotionFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 营销分页查询执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionPageQueryExe {
    private final PromotionRootGateway promotionRootGateway;

    /**
     * 执行营销分页查询。
     */
    @Transactional(readOnly = true)
    public PromotionPageDTO execute(PromotionPageQry q) {
        var f = new PromotionFilter(q.pageNum(), q.pageSize(), q.state(), q.code());
        return new PromotionPageDTO(
                promotionRootGateway.page(f).stream().map(PromotionDtoMapper::result).toList(),
                promotionRootGateway.count(f));
    }
}

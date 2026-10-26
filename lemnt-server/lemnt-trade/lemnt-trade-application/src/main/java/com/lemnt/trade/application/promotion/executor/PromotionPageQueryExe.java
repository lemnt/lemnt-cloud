package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.dto.*;
import com.lemnt.trade.application.promotion.qry.PromotionPageQry;
import com.lemnt.trade.application.promotion.service.PromotionDtoMapper;
import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.model.PromotionFilter;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionPageQueryExe {
    private final PromotionGateway gateway;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PromotionPageDTO execute(PromotionPageQry q) {
        var f = new PromotionFilter(q.pageNum(), q.pageSize(), q.state(), q.code());
        return new PromotionPageDTO(
                gateway.page(f).stream().map(PromotionDtoMapper::result).toList(),
                gateway.count(f));
    }
}

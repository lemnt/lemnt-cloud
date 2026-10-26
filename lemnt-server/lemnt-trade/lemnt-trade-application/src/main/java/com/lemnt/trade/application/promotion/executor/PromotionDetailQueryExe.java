package com.lemnt.trade.application.promotion.executor;

import com.lemnt.trade.application.promotion.dto.*;
import com.lemnt.trade.application.promotion.service.PromotionDtoMapper;
import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionDetailQueryExe {
    private final PromotionGateway gateway;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PromotionDetailDTO execute(long id) {
        PricingException.require(id > 0);
        var r = gateway.root(id);
        if (r == null)
            throw new PromotionPersistenceException(
                    PromotionPersistenceException.Code.PROMOTION_NOT_FOUND);
        PromotionDraft active =
                r.activeVersion() == null ? null : gateway.version(id, r.activeVersion()).draft();
        long now = gateway.nowEpochMs();
        String phase =
                active == null
                        ? null
                        : now < active.startsAtEpochMs()
                                ? "UPCOMING"
                                : now >= active.endsAtEpochMs() ? "ENDED" : "RUNNING";
        return new PromotionDetailDTO(
                id,
                Math.toIntExact(r.revision()),
                r.state().name(),
                r.activeVersion(),
                r.code(),
                PromotionDtoMapper.draft(r.draft()),
                PromotionDtoMapper.draft(active),
                phase);
    }
}

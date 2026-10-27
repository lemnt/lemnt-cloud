package com.lemnt.mall.application.promotion.executor;

import com.lemnt.mall.application.promotion.dto.PromotionDetailDTO;
import com.lemnt.mall.application.promotion.service.PromotionDtoMapper;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionPersistenceException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 营销详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class PromotionDetailQueryExe {
    private final PromotionRootGateway promotionRootGateway;
    private final PromotionVersionGateway promotionVersionGateway;

    /**
     * 执行营销详情查询。
     */
    @Transactional(readOnly = true)
    public PromotionDetailDTO execute(long id) {
        PricingException.require(id > 0);
        var r = promotionRootGateway.root(id);
        if (r == null) {
            throw new PromotionPersistenceException(
                    PromotionPersistenceException.Code.PROMOTION_NOT_FOUND);
        }
        PromotionDraft active =
                r.activeVersion() == null ? null : promotionVersionGateway.version(id, r.activeVersion()).draft();
        long now = promotionRootGateway.nowEpochMs();
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

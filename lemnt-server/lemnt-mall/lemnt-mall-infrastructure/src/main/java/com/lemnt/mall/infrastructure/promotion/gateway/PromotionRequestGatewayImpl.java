package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionRequestGateway;
import com.lemnt.mall.domain.promotion.model.PromotionReceipt;
import com.lemnt.mall.domain.promotion.model.PromotionRequest;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionRootDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionRequestMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionRequest 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionRequestGatewayImpl implements PromotionRequestGateway {
    private final PromotionRequestMapper promotionRequestMapper;

    @Override
    /**
     * 锁定后台请求。
     */
    public PromotionRequest lockRequest(long o, String q, String a, String f) {
        promotionRequestMapper.ensureRequest(o, q, a, f);
        var r = promotionRequestMapper.lockRequest(o, q);
        return new PromotionRequest(
                r.getAction(),
                r.getFingerprint(),
                r.getResultId() == null
                ? null
                : new PromotionReceipt(
                r.getResultId(),
                r.getResultRevision(),
                PromotionRoot.State.valueOf(r.getResultState()),
                r.getResultActiveVersion(),
                r.getResultLastVersion()));
    }

    @Override
    /**
     * 完成后台配置请求。
     */
    public void completeRequest(long o, String q, PromotionReceipt r) {
        var d = new PromotionRootDO();
        d.setId(r.id());
        d.setRevision(r.revision());
        d.setState(r.state().name());
        d.setActiveVersion(r.activeVersion());
        d.setLastVersion(r.lastVersion());
        one(promotionRequestMapper.completeRequest(o, q, d));
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }
}

package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PublishedPromotion;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionVersionDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionVersionMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionVersion 表持久化实现。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionVersionGatewayImpl implements PromotionVersionGateway {
    private final PromotionVersionMapper promotionVersionMapper;

    @Override
    /**
     * 版本。
     */
    public PublishedPromotion version(long id, int n) {
        return PromotionConverter.version(promotionVersionMapper.version(id, n));
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 写入促销版本主体，范围由应用层编排。 */
    @Override
    /**
     * 插入版本。
     */
    public void insertVersion(PublishedPromotion v) {
        var r = new PromotionVersionDO();
        r.setRootId(v.rootId());
        r.setVersion(v.version());
        r.setDraftJson(PromotionConverter.encode(v.draft()));
        r.setRuleHash(v.ruleHash());
        r.setAlgorithmVersion(v.algorithmVersion());
        r.setStartsAtEpochMs(v.draft().startsAtEpochMs());
        r.setEndsAtEpochMs(v.draft().endsAtEpochMs());
        r.setOperatorId(v.operatorId());
        r.setPublishedAtEpochMs(v.publishedAtEpochMs());
        one(promotionVersionMapper.insertVersion(r));
    }
}

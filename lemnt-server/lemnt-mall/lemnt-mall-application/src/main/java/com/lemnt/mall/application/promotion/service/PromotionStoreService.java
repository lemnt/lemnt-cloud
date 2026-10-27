package com.lemnt.mall.application.promotion.service;

import com.lemnt.mall.domain.promotion.gateway.PromotionCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionMemberCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionScopeGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionUsageEventGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionUsageGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.domain.promotion.model.PromotionUsage;
import com.lemnt.mall.domain.promotion.model.PublishedPromotion;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** PromotionStoreService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionStoreService {
    private final PromotionRootGateway promotionRootGateway;
    private final PromotionCounterGateway promotionCounterGateway;
    private final PromotionVersionGateway promotionVersionGateway;
    private final PromotionScopeGateway promotionScopeGateway;
    private final PromotionUsageGateway promotionUsageGateway;
    private final PromotionUsageEventGateway promotionUsageEventGateway;
    private final PromotionMemberCounterGateway promotionMemberCounterGateway;

    /** 按既有闭合文档格式计算草稿摘要。 */
    public String draftHash(PromotionDraft value) {
        return promotionRootGateway.draftHash(value);
    }

    /** 先创建促销主体，再初始化额度计数器。 */
    public PromotionRoot createRoot(String code, PromotionDraft value, long operatorId) {
        long id = promotionRootGateway.insertRoot(code, value, operatorId);
        promotionCounterGateway.initialize(id);
        return promotionRootGateway.root(id);
    }

    /** 保存不可变版本及其范围快照。 */
    public void insertVersion(PublishedPromotion value) {
        promotionVersionGateway.insertVersion(value);
        promotionScopeGateway.saveScope(value);
    }

    /** 保存占用记录及对应状态事件。 */
    public void insertUsage(PromotionUsage value) {
        long now = promotionRootGateway.nowEpochMs();
        promotionUsageGateway.insertUsage(value, now);
        promotionUsageEventGateway.append(value.orderNo(), value.rootId(), null, "HELD", "ORDER_RESERVED", now);
    }

    /** 按全局、会员、占用记录、事件的原顺序结束额度占用。 */
    public void finishUsage(PromotionUsage value, PromotionUsage.State state, String reason) {
        long now = promotionRootGateway.nowEpochMs();
        promotionCounterGateway.finish(value.rootId(), state.name());
        promotionMemberCounterGateway.finish(value.rootId(), value.memberId(), state.name());
        promotionUsageGateway.finishUsage(value, state, now);
        promotionUsageEventGateway.append(value.orderNo(), value.rootId(), "HELD", state.name(), reason, now);
    }
}

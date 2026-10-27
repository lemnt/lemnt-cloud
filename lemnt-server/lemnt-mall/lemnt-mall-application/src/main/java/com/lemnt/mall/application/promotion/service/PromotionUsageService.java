package com.lemnt.mall.application.promotion.service;

import com.lemnt.mall.application.promotion.service.PromotionStoreService;
import com.lemnt.mall.domain.order.gateway.OrderGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionMemberCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionUsageGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionPersistenceException;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.domain.promotion.model.PromotionUsage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.lemnt.mall.domain.promotion.model.PromotionPersistenceException.Code.*;

/**
 * 商城促销额度在本地事务内维护，订单状态通过交易服务查询。
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
@lombok.RequiredArgsConstructor
public class PromotionUsageService {
    private final PromotionCounterGateway promotionCounterGateway;
    private final PromotionMemberCounterGateway promotionMemberCounterGateway;
    private final PromotionRootGateway promotionRootGateway;
    private final PromotionStoreService promotionStoreService;
    private final PromotionUsageGateway promotionUsageGateway;
    private final PromotionVersionGateway promotionVersionGateway;
    private final OrderGateway orderGateway;

    /**
     * 创建营销使用服务实例。
     */

    /**
     * 占用。
     */
    public PromotionUsage hold(
            String no, long member, long id, int version, long discount, String hash) {
        check(no, id);
        PricingException.require(
                member > 0
                && version > 0
                && discount > 0
                && hash != null
                && hash.matches("[a-f0-9]{64}"));
        var order = orderGateway.findByOrderNo(no);
        if (order == null || order.memberId() != member) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        var root = promotionRootGateway.lockRoot(id);
        if (root == null) {
            throw error(PROMOTION_NOT_FOUND);
        }
        var counter = promotionCounterGateway.lockCounter(id);
        promotionMemberCounterGateway.lockMemberCounter(id, member);
        var old = promotionUsageGateway.lockUsage(no, id);
        if (old != null) {
            if (old.memberId() != member
                    || old.version() != version
                    || old.discountFen() != discount
                    || !old.ruleHash().equals(hash)) {
                throw error(PROMOTION_USAGE_CONFLICT);
            }
            return old;
        }
        if (!"RESERVING".equals(order.status())) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        if (root.state() != PromotionRoot.State.PUBLISHED
                || root.activeVersion() == null
                || root.activeVersion() != version) {
            throw error(PROMOTION_SNAPSHOT_STALE);
        }
        var published = promotionVersionGateway.version(id, version);
        if (published == null
                || !published.ruleHash().equals(hash)
                || !published.draft().version(id, version).activeAt(promotionRootGateway.nowEpochMs())) {
            throw error(PROMOTION_SNAPSHOT_STALE);
        }
        if (!promotionCounterGateway.takeGlobal(id) || !promotionMemberCounterGateway.takeMember(id,
                member,
                counter.memberLimit())) {
            throw error(PROMOTION_QUOTA_CHANGED);
        }
        var u =
                new PromotionUsage(
                no, id, version, member, discount, hash, PromotionUsage.State.HELD);
        promotionStoreService.insertUsage(u);
        return u;
    }

    /**
     * 确认。
     */
    public PromotionUsage confirm(String no, long id, String reason) {
        return finish(no, id, PromotionUsage.State.USED, reason);
    }

    /**
     * 释放。
     */
    public PromotionUsage release(String no, long id, String reason) {
        return finish(no, id, PromotionUsage.State.RELEASED, reason);
    }

    private PromotionUsage finish(String no, long id, PromotionUsage.State target, String reason) {
        check(no, id);
        PricingException.require(reason != null && !reason.isBlank() && reason.length() <= 100);
        var order = orderGateway.findByOrderNo(no);
        if (order == null) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        if (promotionRootGateway.lockRoot(id) == null) {
            throw error(PROMOTION_NOT_FOUND);
        }
        promotionCounterGateway.lockCounter(id);
        promotionMemberCounterGateway.lockMemberCounter(id, order.memberId());
        var u = promotionUsageGateway.lockUsage(no, id);
        if (u == null || u.memberId() != order.memberId()) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        boolean allowed =
                target == PromotionUsage.State.USED
                ? Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                : "CLOSED".equals(order.status());
        if (!allowed) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        if (u.state() == target) {
            return u;
        }
        if (u.state() != PromotionUsage.State.HELD) {
            throw error(PROMOTION_USAGE_CONFLICT);
        }
        promotionStoreService.finishUsage(u, target, reason);
        return new PromotionUsage(
                no, id, u.version(), u.memberId(), u.discountFen(), u.ruleHash(), target);
    }

    private static void check(String no, long id) {
        PricingException.require(no != null && no.matches("[A-Za-z0-9_-]{1,40}") && id > 0);
    }

    private static PromotionPersistenceException error(PromotionPersistenceException.Code c) {
        return new PromotionPersistenceException(c);
    }
}

package com.lemnt.trade.application.promotion.service;

import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.Set;
import static com.lemnt.trade.domain.promotion.model.PromotionPersistenceException.Code.*;

/** Requires the outer order/coupon/outbox transaction; does not authorize payment or closure. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionUsageService {
    private final PromotionGateway gateway;

    public PromotionUsageService(PromotionGateway g) {
        gateway = g;
    }

    public PromotionUsage hold(
            String no, long member, long id, int version, long discount, String hash) {
        check(no, id);
        PricingException.require(
                member > 0
                        && version > 0
                        && discount > 0
                        && hash != null
                        && hash.matches("[a-f0-9]{64}"));
        var order = gateway.lockOrder(no);
        if (order == null || order.memberId() != member) throw error(PROMOTION_USAGE_CONFLICT);
        var root = gateway.lockRoot(id);
        if (root == null) throw error(PROMOTION_NOT_FOUND);
        var counter = gateway.lockCounter(id);
        gateway.lockMemberCounter(id, member);
        var old = gateway.lockUsage(no, id);
        if (old != null) {
            if (old.memberId() != member
                    || old.version() != version
                    || old.discountFen() != discount
                    || !old.ruleHash().equals(hash)) throw error(PROMOTION_USAGE_CONFLICT);
            return old;
        }
        if (!"RESERVING".equals(order.status())) throw error(PROMOTION_USAGE_CONFLICT);
        if (root.state() != PromotionRoot.State.PUBLISHED
                || root.activeVersion() == null
                || root.activeVersion() != version) throw error(PROMOTION_SNAPSHOT_STALE);
        var published = gateway.version(id, version);
        if (published == null
                || !published.ruleHash().equals(hash)
                || !published.draft().version(id, version).activeAt(gateway.nowEpochMs()))
            throw error(PROMOTION_SNAPSHOT_STALE);
        if (!gateway.takeGlobal(id) || !gateway.takeMember(id, member, counter.memberLimit()))
            throw error(PROMOTION_QUOTA_CHANGED);
        var u =
                new PromotionUsage(
                        no, id, version, member, discount, hash, PromotionUsage.State.HELD);
        gateway.insertUsage(u);
        return u;
    }

    public PromotionUsage confirm(String no, long id, String reason) {
        return finish(no, id, PromotionUsage.State.USED, reason);
    }

    public PromotionUsage release(String no, long id, String reason) {
        return finish(no, id, PromotionUsage.State.RELEASED, reason);
    }

    private PromotionUsage finish(String no, long id, PromotionUsage.State target, String reason) {
        check(no, id);
        PricingException.require(reason != null && !reason.isBlank() && reason.length() <= 100);
        var order = gateway.lockOrder(no);
        if (order == null) throw error(PROMOTION_USAGE_CONFLICT);
        if (gateway.lockRoot(id) == null) throw error(PROMOTION_NOT_FOUND);
        gateway.lockCounter(id);
        gateway.lockMemberCounter(id, order.memberId());
        var u = gateway.lockUsage(no, id);
        if (u == null || u.memberId() != order.memberId()) throw error(PROMOTION_USAGE_CONFLICT);
        boolean allowed =
                target == PromotionUsage.State.USED
                        ? Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                        : "CLOSED".equals(order.status());
        if (!allowed) throw error(PROMOTION_USAGE_CONFLICT);
        if (u.state() == target) return u;
        if (u.state() != PromotionUsage.State.HELD) throw error(PROMOTION_USAGE_CONFLICT);
        gateway.finishUsage(u, target, reason);
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

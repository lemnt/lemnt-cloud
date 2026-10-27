package com.lemnt.mall.application.promotion.service;

import com.lemnt.mall.application.promotion.service.PromotionStoreService;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.promotion.gateway.PromotionCatalogFactsGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionMemberCounterGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRequestGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionRootGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionVersionGateway;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PromotionDraft;
import com.lemnt.mall.domain.promotion.model.PromotionPersistenceException;
import com.lemnt.mall.domain.promotion.model.PromotionReceipt;
import com.lemnt.mall.domain.promotion.model.PromotionRoot;
import com.lemnt.mall.domain.promotion.model.PublishedPromotion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.lemnt.mall.domain.promotion.model.PromotionPersistenceException.Code.*;

/**
 * Internal orchestration. T3 must authorize operator and validate scope with authoritative RPC.
 */
@Service
@Transactional
@lombok.RequiredArgsConstructor
public class PromotionManagementService {
    private final PromotionCounterGateway promotionCounterGateway;
    private final PromotionMemberCounterGateway promotionMemberCounterGateway;
    private final PromotionRequestGateway promotionRequestGateway;
    private final PromotionRootGateway promotionRootGateway;
    private final PromotionStoreService promotionStoreService;
    private final PromotionVersionGateway promotionVersionGateway;
    private final PromotionCatalogFactsGateway promotionCatalogFactsGateway;

    /**
     * 创建营销管理服务实例。
     */

    /**
     * 创建。
     */
    public PromotionReceipt create(long o, String q, String code, PromotionDraft d) {
        checkRequest(o, q);
        PricingException.require(code != null && code.matches("[A-Za-z0-9_-]{1,64}") && d != null);
        var replay =
                replay(
                o,
                q,
                "CREATE",
                CheckoutRules.fingerprint("CREATE|" + code + "|" + promotionStoreService.draftHash(d)));
        if (replay != null) {
            return replay;
        }
        promotionCatalogFactsGateway.validateScope(d.scope());
        var r = promotionStoreService.createRoot(code, d, o);
        return complete(o, q, r);
    }

    /**
     * 保存草稿。
     */
    public PromotionReceipt saveDraft(long o, String q, long id, long rev, PromotionDraft d) {
        checkRequest(o, q);
        identity(id, rev);
        PricingException.require(d != null);
        var replay =
                replay(
                o,
                q,
                "DRAFT",
                CheckoutRules.fingerprint(
                "DRAFT|" + id + "|" + rev + "|" + promotionStoreService.draftHash(d)));
        if (replay != null) {
            return replay;
        }
        var r = editable(id, rev);
        promotionCatalogFactsGateway.validateScope(d.scope());
        var next =
                new PromotionRoot(
                id,
                r.code(),
                r.state(),
                nextRevision(rev),
                r.activeVersion(),
                r.lastVersion(),
                d);
        promotionRootGateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    /**
     * 发布。
     */
    public PromotionReceipt publish(long o, String q, long id, long rev) {
        checkRequest(o, q);
        identity(id, rev);
        var replay = replay(o, q, "PUBLISH", fingerprint("PUBLISH", id, rev));
        if (replay != null) {
            return replay;
        }
        var r = editable(id, rev);
        var d = r.draft();
        long now = promotionRootGateway.nowEpochMs();
        if (d.endsAtEpochMs() <= now) {
            throw error(PROMOTION_STATE_CONFLICT);
        }
        promotionCatalogFactsGateway.validateScope(d.scope());
        var c = promotionCounterGateway.lockCounter(id);
        if (d.globalUseLimit() != null && c.held() + c.used() > d.globalUseLimit()
                || d.memberUseLimit() != null
                && promotionMemberCounterGateway.largestMemberUsage(id) > d.memberUseLimit()) {
            throw error(PROMOTION_LIMIT_BELOW_USAGE);
        }
        int v = Math.incrementExact(r.lastVersion());
        promotionStoreService.insertVersion(
                new PublishedPromotion(
                id,
                v,
                d,
                CheckoutRules.fingerprint(id + ":" + v + ":" + promotionStoreService.draftHash(d)),
                "promo-1",
                o,
                now));
        promotionCounterGateway.setLimits(id, d.globalUseLimit(), d.memberUseLimit());
        var next =
                new PromotionRoot(
                id, r.code(), PromotionRoot.State.PUBLISHED, nextRevision(rev), v, v, d);
        promotionRootGateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    /**
     * 修改状态。
     */
    public PromotionReceipt changeState(
            long o, String q, long id, long rev, PromotionRoot.State target) {
        checkRequest(o, q);
        identity(id, rev);
        PricingException.require(target != null && target != PromotionRoot.State.DRAFT);
        var replay = replay(o, q, target.name(), fingerprint(target.name(), id, rev));
        if (replay != null) {
            return replay;
        }
        var r = editable(id, rev);
        boolean valid =
                target == PromotionRoot.State.ARCHIVED
                || target == PromotionRoot.State.PAUSED
                && r.state() == PromotionRoot.State.PUBLISHED
                || target == PromotionRoot.State.PUBLISHED
                && r.state() == PromotionRoot.State.PAUSED;
        if (!valid) {
            throw error(PROMOTION_STATE_CONFLICT);
        }
        if (target == PromotionRoot.State.PUBLISHED
                && promotionVersionGateway.version(id, r.activeVersion()).draft().endsAtEpochMs()
                <= promotionRootGateway.nowEpochMs()) {
            throw error(PROMOTION_STATE_CONFLICT);
        }
        var next =
                new PromotionRoot(
                id,
                r.code(),
                target,
                nextRevision(rev),
                r.activeVersion(),
                r.lastVersion(),
                r.draft());
        promotionRootGateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    private PromotionReceipt complete(long o, String q, PromotionRoot r) {
        promotionRequestGateway.completeRequest(o, q, r.receipt());
        return r.receipt();
    }

    private PromotionReceipt replay(long o, String q, String a, String f) {
        var r = promotionRequestGateway.lockRequest(o, q, a, f);
        if (!r.action().equals(a) || !r.fingerprint().equals(f)) {
            throw error(ADMIN_REQUEST_CONFLICT);
        }
        return r.receipt();
    }

    private PromotionRoot editable(long id, long rev) {
        var r = promotionRootGateway.lockRoot(id);
        if (r == null) {
            throw error(PROMOTION_NOT_FOUND);
        }
        if (r.revision() != rev) {
            throw error(PROMOTION_REVISION_CONFLICT);
        }
        if (r.state() == PromotionRoot.State.ARCHIVED) {
            throw error(PROMOTION_STATE_CONFLICT);
        }
        return r;
    }

    private static int nextRevision(long revision) {
        if (revision >= Integer.MAX_VALUE) {
            throw error(PROMOTION_REVISION_CONFLICT);
        }
        return Math.toIntExact(revision + 1);
    }

    private static String fingerprint(String a, long id, long rev) {
        return CheckoutRules.fingerprint(a + "|" + id + "|" + rev);
    }

    private static void checkRequest(long o, String q) {
        PricingException.require(o > 0 && q != null && q.matches("[A-Za-z0-9_-]{1,64}"));
    }

    private static void identity(long id, long rev) {
        PricingException.require(id > 0 && rev >= 0);
    }

    private static PromotionPersistenceException error(PromotionPersistenceException.Code c) {
        return new PromotionPersistenceException(c);
    }
}

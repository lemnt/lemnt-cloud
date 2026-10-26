package com.lemnt.trade.application.promotion.service;

import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.gateway.PromotionCatalogFactsGateway;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.lemnt.trade.domain.promotion.model.PromotionPersistenceException.Code.*;

/** Internal orchestration. T3 must authorize operator and validate scope with authoritative RPC. */
@Service
@Transactional
public class PromotionAdministrationService {
    private final PromotionGateway gateway;
    private final PromotionCatalogFactsGateway facts;

    public PromotionAdministrationService(PromotionGateway g, PromotionCatalogFactsGateway facts) {
        gateway = g;
        this.facts = facts;
    }

    public PromotionAdminReceipt create(long o, String q, String code, PromotionDraft d) {
        checkRequest(o, q);
        PricingException.require(code != null && code.matches("[A-Za-z0-9_-]{1,64}") && d != null);
        var replay =
                replay(
                        o,
                        q,
                        "CREATE",
                        CheckoutRules.fingerprint("CREATE|" + code + "|" + gateway.draftHash(d)));
        if (replay != null) return replay;
        facts.validateScope(d.scope());
        var r = gateway.createRoot(code, d, o);
        return complete(o, q, r);
    }

    public PromotionAdminReceipt saveDraft(long o, String q, long id, long rev, PromotionDraft d) {
        checkRequest(o, q);
        identity(id, rev);
        PricingException.require(d != null);
        var replay =
                replay(
                        o,
                        q,
                        "DRAFT",
                        CheckoutRules.fingerprint(
                                "DRAFT|" + id + "|" + rev + "|" + gateway.draftHash(d)));
        if (replay != null) return replay;
        var r = editable(id, rev);
        facts.validateScope(d.scope());
        var next =
                new PromotionRoot(
                        id,
                        r.code(),
                        r.state(),
                        nextRevision(rev),
                        r.activeVersion(),
                        r.lastVersion(),
                        d);
        gateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    public PromotionAdminReceipt publish(long o, String q, long id, long rev) {
        checkRequest(o, q);
        identity(id, rev);
        var replay = replay(o, q, "PUBLISH", fingerprint("PUBLISH", id, rev));
        if (replay != null) return replay;
        var r = editable(id, rev);
        var d = r.draft();
        long now = gateway.nowEpochMs();
        if (d.endsAtEpochMs() <= now) throw error(PROMOTION_STATE_CONFLICT);
        facts.validateScope(d.scope());
        var c = gateway.lockCounter(id);
        if (d.globalUseLimit() != null && c.held() + c.used() > d.globalUseLimit()
                || d.memberUseLimit() != null
                        && gateway.largestMemberUsage(id) > d.memberUseLimit())
            throw error(PROMOTION_LIMIT_BELOW_USAGE);
        int v = Math.incrementExact(r.lastVersion());
        gateway.insertVersion(
                new PublishedPromotion(
                        id,
                        v,
                        d,
                        CheckoutRules.fingerprint(id + ":" + v + ":" + gateway.draftHash(d)),
                        "promo-1",
                        o,
                        now));
        gateway.setLimits(id, d.globalUseLimit(), d.memberUseLimit());
        var next =
                new PromotionRoot(
                        id, r.code(), PromotionRoot.State.PUBLISHED, nextRevision(rev), v, v, d);
        gateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    public PromotionAdminReceipt changeState(
            long o, String q, long id, long rev, PromotionRoot.State target) {
        checkRequest(o, q);
        identity(id, rev);
        PricingException.require(target != null && target != PromotionRoot.State.DRAFT);
        var replay = replay(o, q, target.name(), fingerprint(target.name(), id, rev));
        if (replay != null) return replay;
        var r = editable(id, rev);
        boolean valid =
                target == PromotionRoot.State.ARCHIVED
                        || target == PromotionRoot.State.PAUSED
                                && r.state() == PromotionRoot.State.PUBLISHED
                        || target == PromotionRoot.State.PUBLISHED
                                && r.state() == PromotionRoot.State.PAUSED;
        if (!valid) throw error(PROMOTION_STATE_CONFLICT);
        if (target == PromotionRoot.State.PUBLISHED
                && gateway.version(id, r.activeVersion()).draft().endsAtEpochMs()
                        <= gateway.nowEpochMs()) throw error(PROMOTION_STATE_CONFLICT);
        var next =
                new PromotionRoot(
                        id,
                        r.code(),
                        target,
                        nextRevision(rev),
                        r.activeVersion(),
                        r.lastVersion(),
                        r.draft());
        gateway.saveRoot(next, rev, o);
        return complete(o, q, next);
    }

    private PromotionAdminReceipt complete(long o, String q, PromotionRoot r) {
        gateway.completeAdminRequest(o, q, r.receipt());
        return r.receipt();
    }

    private PromotionAdminReceipt replay(long o, String q, String a, String f) {
        var r = gateway.lockAdminRequest(o, q, a, f);
        if (!r.action().equals(a) || !r.fingerprint().equals(f))
            throw error(ADMIN_REQUEST_CONFLICT);
        return r.receipt();
    }

    private PromotionRoot editable(long id, long rev) {
        var r = gateway.lockRoot(id);
        if (r == null) throw error(PROMOTION_NOT_FOUND);
        if (r.revision() != rev) throw error(PROMOTION_REVISION_CONFLICT);
        if (r.state() == PromotionRoot.State.ARCHIVED) throw error(PROMOTION_STATE_CONFLICT);
        return r;
    }

    private static int nextRevision(long revision) {
        if (revision >= Integer.MAX_VALUE) throw error(PROMOTION_REVISION_CONFLICT);
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

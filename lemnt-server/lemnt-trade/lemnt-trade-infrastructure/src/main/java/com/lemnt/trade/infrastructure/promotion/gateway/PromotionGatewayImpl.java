package com.lemnt.trade.infrastructure.promotion.gateway;

import com.lemnt.trade.domain.promotion.gateway.PromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.trade.infrastructure.promotion.dataobject.*;
import com.lemnt.trade.infrastructure.promotion.mapper.PromotionMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class PromotionGatewayImpl implements PromotionGateway {
    private final PromotionMapper mapper;

    public PromotionGatewayImpl(PromotionMapper mapper) {
        this.mapper = mapper;
    }

    public java.util.List<PromotionAdminReceipt> page(PromotionFilter filter) {
        return mapper.page(filter).stream()
                .map(
                        r ->
                                new PromotionAdminReceipt(
                                        r.getId(),
                                        r.getRevision(),
                                        PromotionRoot.State.valueOf(r.getState()),
                                        r.getActiveVersion(),
                                        r.getLastVersion()))
                .toList();
    }

    public long count(PromotionFilter filter) {
        return mapper.count(filter);
    }

    public long nowEpochMs() {
        return mapper.nowEpochMs();
    }

    public String draftHash(PromotionDraft d) {
        return PromotionConverter.hash(d);
    }

    public PromotionAdminRequest lockAdminRequest(long o, String q, String a, String f) {
        mapper.ensureRequest(o, q, a, f);
        var r = mapper.lockRequest(o, q);
        return new PromotionAdminRequest(
                r.getAction(),
                r.getFingerprint(),
                r.getResultId() == null
                        ? null
                        : new PromotionAdminReceipt(
                                r.getResultId(),
                                r.getResultRevision(),
                                PromotionRoot.State.valueOf(r.getResultState()),
                                r.getResultActiveVersion(),
                                r.getResultLastVersion()));
    }

    public void completeAdminRequest(long o, String q, PromotionAdminReceipt r) {
        var d = new PromotionRootDO();
        d.setId(r.id());
        d.setRevision(r.revision());
        d.setState(r.state().name());
        d.setActiveVersion(r.activeVersion());
        d.setLastVersion(r.lastVersion());
        one(mapper.completeRequest(o, q, d));
    }

    public PromotionRoot createRoot(String code, PromotionDraft draft, long o) {
        var r = new PromotionRootDO();
        r.setCode(code);
        r.setDraftJson(PromotionConverter.encode(draft));
        one(mapper.insertRoot(r, o));
        one(mapper.insertCounter(r.getId()));
        return root(r.getId());
    }

    public PromotionRoot root(long id) {
        return PromotionConverter.root(mapper.root(id));
    }

    public PromotionRoot lockRoot(long id) {
        return PromotionConverter.root(mapper.lockRoot(id));
    }

    public void saveRoot(PromotionRoot r, long e, long o) {
        one(mapper.saveRoot(PromotionConverter.row(r), e, o));
    }

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
        one(mapper.insertVersion(r));
        for (long id : v.draft().scope().ids())
            one(mapper.insertScope(v.rootId(), v.version(), v.draft().scope().kind().name(), id));
    }

    public PublishedPromotion version(long id, int n) {
        return PromotionConverter.version(mapper.version(id, n));
    }

    public PromotionCounter lockCounter(long id) {
        var r = mapper.lockCounter(id);
        if (r == null) throw new IllegalStateException("Missing promotion counter");
        return new PromotionCounter(
                r.getHeldCount(), r.getUsedCount(), r.getGlobalLimit(), r.getMemberLimit());
    }

    public long largestMemberUsage(long id) {
        Long n = mapper.largestMemberUsage(id);
        return n == null ? 0 : n;
    }

    public void setLimits(long id, Long g, Long m) {
        one(mapper.setLimits(id, g, m));
    }

    public void lockMemberCounter(long id, long m) {
        mapper.ensureMemberCounter(id, m);
        if (mapper.lockMemberCounter(id, m) == null)
            throw new IllegalStateException("Missing member counter");
    }

    public boolean takeGlobal(long id) {
        return mapper.takeGlobal(id) == 1;
    }

    public boolean takeMember(long id, long m, Long l) {
        return mapper.takeMember(id, m, l) == 1;
    }

    public PromotionOrder lockOrder(String n) {
        var r = mapper.lockOrder(n);
        return r == null
                ? null
                : new PromotionOrder(r.getOrderNo(), r.getMemberId(), r.getStatus());
    }

    public PromotionUsage lockUsage(String n, long id) {
        return PromotionConverter.usage(mapper.lockUsage(n, id));
    }

    public void insertUsage(PromotionUsage u) {
        long n = nowEpochMs();
        one(mapper.insertUsage(PromotionConverter.row(u), n));
        one(mapper.event(u.orderNo(), u.rootId(), null, "HELD", "ORDER_RESERVED", n));
    }

    public void finishUsage(PromotionUsage u, PromotionUsage.State s, String reason) {
        long n = nowEpochMs();
        one(mapper.finishGlobal(u.rootId(), s.name()));
        one(mapper.finishMember(u.rootId(), u.memberId(), s.name()));
        one(mapper.finishUsage(u.orderNo(), u.rootId(), s.name(), n));
        one(mapper.event(u.orderNo(), u.rootId(), "HELD", s.name(), reason, n));
    }

    private static void one(int n) {
        if (n != 1) throw new IllegalStateException("Promotion persistence CAS/integrity failure");
    }
}

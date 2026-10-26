package com.lemnt.trade.domain.promotion.gateway;

import com.lemnt.trade.domain.promotion.model.*;

/** Transactional primitives. Caller owns the documented order and resource transaction. */
public interface PromotionGateway {
    java.util.List<PromotionAdminReceipt> page(PromotionFilter filter);

    long count(PromotionFilter filter);

    long nowEpochMs();

    String draftHash(PromotionDraft draft);

    PromotionAdminRequest lockAdminRequest(
            long operator, String request, String action, String fingerprint);

    void completeAdminRequest(long operator, String request, PromotionAdminReceipt receipt);

    PromotionRoot createRoot(String code, PromotionDraft draft, long operator);

    PromotionRoot root(long id);

    PromotionRoot lockRoot(long id);

    void saveRoot(PromotionRoot root, long expected, long operator);

    void insertVersion(PublishedPromotion version);

    PublishedPromotion version(long root, int version);

    PromotionCounter lockCounter(long id);

    long largestMemberUsage(long id);

    void setLimits(long id, Long global, Long member);

    void lockMemberCounter(long id, long member);

    boolean takeGlobal(long id);

    boolean takeMember(long id, long member, Long limit);

    PromotionOrder lockOrder(String order);

    PromotionUsage lockUsage(String order, long id);

    void insertUsage(PromotionUsage usage);

    void finishUsage(PromotionUsage usage, PromotionUsage.State target, String reason);
}

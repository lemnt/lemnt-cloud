package com.lemnt.mall.domain.promotion.gateway;

/** PromotionMemberCounter 表领域存储接口。 */
public interface PromotionMemberCounterGateway {
    /**
     * 最大值会员使用。
     */
    long largestMemberUsage(long id);

    /**
     * 锁定会员计数器。
     */
    void lockMemberCounter(long id, long member);

    /**
     * 获取会员。
     */
    boolean takeMember(long id, long member, Long limit);

    /** 结束会员占用额度。 */
    void finish(long id, long memberId, String state);
}

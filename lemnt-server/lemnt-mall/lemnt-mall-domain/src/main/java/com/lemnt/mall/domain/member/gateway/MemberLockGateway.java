package com.lemnt.mall.domain.member.gateway;

/** 会员操作的跨实例互斥边界，不存储会员账户。 */
public interface MemberLockGateway {
    /** 获取会员操作锁，持有至当前事务提交或回滚完成。 */
    void lockMember(Long memberId);
}

package com.lemnt.member.domain.growth.gateway;

/** 成长配置事务锁，不借用任何业务配置行作为锁。 */
public interface MemberConfigLockGateway {
    /** 串行化等级规则及积分配置，覆盖事务提交或回滚。 */
    void lockConfiguration();
}

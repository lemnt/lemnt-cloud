package com.lemnt.mall.domain.member.model;

/** 结算所需会员状态、等级折扣和积分余额。 */
public record MemberSnapshot(Long id, Integer status, Integer discountPercent, Long points) {
    public MemberSnapshot(Long id, Integer status) { this(id, status, 100, 0L); }
    public MemberSnapshot(Long id, Integer status, Integer discountPercent) { this(id, status, discountPercent, 0L); }
}

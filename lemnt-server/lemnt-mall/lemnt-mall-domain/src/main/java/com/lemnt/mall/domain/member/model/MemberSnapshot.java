package com.lemnt.mall.domain.member.model;

/** 会员状态、等级折扣、积分余额及合法公开资料快照。 */
public record MemberSnapshot(Long id, Integer status, Integer discountPercent, Long points, String nickname, String avatar) {
    public MemberSnapshot(Long id, Integer status, Integer discountPercent, Long points) {
        this(id, status, discountPercent, points, null, null);
    }

    public MemberSnapshot(Long id, Integer status) { this(id, status, 100, 0L); }
    public MemberSnapshot(Long id, Integer status, Integer discountPercent) { this(id, status, discountPercent, 0L); }
}

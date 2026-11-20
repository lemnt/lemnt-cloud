package com.lemnt.promotion.domain.member.gateway;

import com.lemnt.promotion.domain.member.model.MemberSnapshot;

/**
 * 会员查询领域网关。
 */
public interface MemberLookupGateway {
    /**
     * 查找按编号。
     */
    MemberSnapshot findById(Long memberId);
}

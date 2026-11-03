package com.lemnt.order.domain.member.gateway;

import com.lemnt.order.domain.member.model.MemberPointConfig;
import com.lemnt.order.domain.member.model.MemberTradeChange;

/** 会员成长权益配置和幂等变更。 */
public interface MemberTradeGateway {
    MemberPointConfig config();

    void apply(MemberTradeChange change);
}

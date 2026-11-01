package com.lemnt.trade.domain.brokerage.gateway;

import com.lemnt.trade.domain.brokerage.model.BrokerageMemberInfo;

/** 会员公开资料查询，只用于分销昵称和头像展示。 */
public interface BrokerageMemberGateway {
    BrokerageMemberInfo find(Long memberId);
}
